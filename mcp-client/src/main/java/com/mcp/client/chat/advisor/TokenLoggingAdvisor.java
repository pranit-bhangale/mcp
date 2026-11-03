package com.mcp.client.chat.advisor;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.metadata.ChatGenerationMetadata;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import reactor.core.publisher.Flux;
import tools.jackson.core.type.TypeReference;
import tools.jackson.core.util.DefaultIndenter;
import tools.jackson.core.util.DefaultPrettyPrinter;
import tools.jackson.core.util.Separators;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Logs token usage grouped by trace: one parent record per user prompt,
 * with every model call (tool-call loop iterations included) as a span.
 * <p>
 * File: {@code track/track-YYYY-MM-DD.json} containing [ {trace}, {trace} ].
 * <p>
 * Trace id resolution:
 * 1. Explicit: {@code .advisors(a -> a.param(TokenLoggingAdvisor.TRACE_ID_KEY, id))}
 * 2. Fallback: same user prompt text within an idle window shares one trace.
 */
public final class TokenLoggingAdvisor implements CallAdvisor, StreamAdvisor {

    public static final String TRACE_ID_KEY = "tokenLog.traceId";

    private static final Logger log = LoggerFactory.getLogger(TokenLoggingAdvisor.class);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final long TRACE_IDLE_MS = Duration.ofMinutes(5).toMillis();

    private static final DefaultPrettyPrinter PRETTY = new DefaultPrettyPrinter()
            .withSeparators(Separators.createDefaultInstance()
                    .withObjectNameValueSpacing(Separators.Spacing.AFTER))
            .withArrayIndenter(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);

    private final JsonMapper jsonMapper = JsonMapper.builder()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .defaultPrettyPrinter(PRETTY)
            .build();
    private final ReentrantLock fileLock = new ReentrantLock();
    private final Map<String, TraceState> activeTraces = new ConcurrentHashMap<>();

    private final Path logDirectory;
    private final String logFilePrefix;
    private final int promptPreviewLength;

    public TokenLoggingAdvisor() {
        this(Paths.get("track"), "track", 200);
    }

    public TokenLoggingAdvisor(Path logDirectory, String logFilePrefix, int promptPreviewLength) {
        this.logDirectory = logDirectory.toAbsolutePath().normalize();
        this.logFilePrefix = logFilePrefix;
        this.promptPreviewLength = Math.max(50, promptPreviewLength);
        try {
            Files.createDirectories(this.logDirectory);
            log.info("Token usage log directory: {}", this.logDirectory);
        } catch (IOException e) {
            log.error("Failed to create log directory: {}", this.logDirectory, e);
        }
    }

    private static boolean hasUsage(ChatResponse cr) {
        Usage u = cr.getMetadata().getUsage();
        u.getTotalTokens();
        return u.getTotalTokens() > 0;
    }

    private static String finishReasonOf(ChatResponse cr) {
        if (cr == null) return null;
        return cr.getResults().stream()
                .map(Generation::getMetadata)
                .map(ChatGenerationMetadata::getFinishReason)
                .filter(s -> s != null && !s.isBlank())
                .findFirst()
                .orElse(null);
    }

    private static int generationCountOf(ChatResponse cr) {
        return cr == null ? 0 : cr.getResults().size();
    }

    private static int nz(Integer i) {
        return i == null ? 0 : i;
    }

    private static long elapsedMs(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000;
    }

    private static String userText(ChatClientRequest request) {
        try {
            String t = request.prompt().getUserMessage().getText();
            return t == null ? "" : t.strip();
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public @NonNull ChatClientResponse adviseCall
            (@NonNull ChatClientRequest request, @NonNull CallAdvisorChain chain) {
        String fullText = userText(request);
        String traceId = resolveTraceId(request, fullText);
        long start = System.nanoTime();

        ChatClientResponse response = chain.nextCall(request);

        ChatResponse cr = response.chatResponse();
        recordSpan(traceId, fullText, cr, finishReasonOf(cr), generationCountOf(cr), false, elapsedMs(start));
        return response;
    }

    @Override
    public @NonNull Flux<ChatClientResponse> adviseStream
            (@NonNull ChatClientRequest request, @NonNull StreamAdvisorChain chain) {
        return Flux.defer(() -> {
            String fullText = userText(request);
            String traceId = resolveTraceId(request, fullText);
            long start = System.nanoTime();

            var usageRef = new AtomicReference<ChatResponse>();
            var finishRef = new AtomicReference<String>();
            var generations = new AtomicInteger();

            return chain.nextStream(request)
                    .doOnNext(r -> {
                        ChatResponse cr = r.chatResponse();
                        if (cr == null) return;
                        if (hasUsage(cr)) usageRef.set(cr);
                        String fr = finishReasonOf(cr);
                        if (fr != null) finishRef.set(fr);
                        generations.accumulateAndGet(generationCountOf(cr), Math::max);
                    })
                    .doOnComplete(() -> recordSpan(traceId, fullText, usageRef.get(),
                            finishRef.get(), generations.get(), true, elapsedMs(start)));
        });
    }

    @Override
    public @NonNull String getName() {
        return "tokenLoggingAdvisor";
    }

    @Override
    public int getOrder() {
        return Integer.MAX_VALUE - 100;
    }

    private String resolveTraceId(ChatClientRequest request, String fullText) {
        Object explicit = request.context().get(TRACE_ID_KEY);
        if (explicit != null) return explicit.toString();
        long now = System.currentTimeMillis();
        activeTraces.values().removeIf(s -> now - s.lastSeenMs() > TRACE_IDLE_MS);
        return activeTraces.compute(fullText, (k, old) -> old == null
                ? new TraceState(UUID.randomUUID().toString(), now)
                : new TraceState(old.traceId(), now)).traceId();
    }

    private void touch(String fullText) {
        activeTraces.computeIfPresent(fullText,
                (k, old) -> new TraceState(old.traceId(), System.currentTimeMillis()));
    }

    private void recordSpan(String traceId, String fullText, ChatResponse cr, String finishReason,
                            int generationCount, boolean streaming, long durationMs) {
        touch(fullText);
        if (cr == null) {
            log.debug("No chat response/usage for traceId={}", traceId);
            return;
        }
        try {
            ChatResponseMetadata meta = cr.getMetadata();
            Usage usage = meta.getUsage();
            String now = ZonedDateTime.now(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            var span = new SpanEntry(
                    0,
                    UUID.randomUUID().toString(),
                    now,
                    durationMs,
                    nz(usage.getPromptTokens()),
                    nz(usage.getCompletionTokens()),
                    nz(usage.getTotalTokens()),
                    generationCount,
                    streaming,
                    meta.getModel(),
                    meta.getId(),
                    finishReason
            );
            upsertTrace(traceId, preview(fullText), now, span);
        } catch (Exception e) {
            log.error("Failed to log token usage for traceId={}", traceId, e); // never break the chat flow
        }
    }

    private String preview(String text) {
        String t = text.replaceAll("\\s+", " ");
        return t.length() > promptPreviewLength ? t.substring(0, promptPreviewLength) + "…" : t;
    }

    private Path todayFile() {
        String day = DATE_FMT.format(ZonedDateTime.now(ZoneId.systemDefault()));
        return logDirectory.resolve(logFilePrefix + "-" + day + ".json");
    }

    /**
     * Adds the span to its trace (creating the trace if new) and rewrites the day file atomically.
     */
    private void upsertTrace(String traceId, String promptPreview, String now, SpanEntry span) throws IOException {
        Path file = todayFile();
        fileLock.lock();
        try {
            Files.createDirectories(logDirectory);
            List<TraceEntry> traces = readTraces(file);

            int idx = -1;
            for (int i = 0; i < traces.size(); i++) {
                if (traces.get(i).traceId().equals(traceId)) {
                    idx = i;
                    break;
                }
            }

            TraceEntry current = idx >= 0
                    ? traces.get(idx)
                    : new TraceEntry(traceId, now, now, promptPreview, 0, 0, 0, 0, List.of());
            TraceEntry updated = current.withSpan(span, now);

            if (idx >= 0) traces.set(idx, updated);
            else traces.add(updated);
            writeTraces(file, traces);
        } finally {
            fileLock.unlock();
        }
    }

    private List<TraceEntry> readTraces(Path file) throws IOException {
        if (!Files.exists(file) || Files.size(file) == 0) return new ArrayList<>();
        try {
            List<TraceEntry> list = jsonMapper.readValue(file.toFile(), new TypeReference<List<TraceEntry>>() {
            });
            return new ArrayList<>(list);
        } catch (Exception e) {
            Path backup = file.resolveSibling(file.getFileName() + ".corrupt-" + System.currentTimeMillis());
            Files.move(file, backup, StandardCopyOption.REPLACE_EXISTING);
            log.warn("Unreadable log file moved to {}", backup, e);
            return new ArrayList<>();
        }
    }

    private void writeTraces(Path file, List<TraceEntry> traces) throws IOException {
        Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
        Files.writeString(tmp, jsonMapper.writeValueAsString(traces) + System.lineSeparator(), StandardCharsets.UTF_8);
        try {
            Files.move(tmp, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (java.nio.file.AtomicMoveNotSupportedException e) {
            Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private record TraceState(String traceId, long lastSeenMs) {
    }

    /**
     * One model call inside a trace.
     */
    public record SpanEntry(
            int spanIndex,
            String spanId,
            String timestamp,
            long durationMs,
            int promptTokens,
            int completionTokens,
            int totalTokens,
            int generationCount,
            boolean streaming,
            String model,
            String responseId,
            String finishReason
    ) {
        SpanEntry withIndex(int index) {
            return new SpanEntry(index, spanId, timestamp, durationMs, promptTokens, completionTokens,
                    totalTokens, generationCount, streaming, model, responseId, finishReason);
        }
    }

    /**
     * Parent record: one user prompt, all its model calls, and summed totals.
     */
    public record TraceEntry(
            String traceId,
            String startedAt,
            String updatedAt,
            String promptPreview,
            int spanCount,
            int promptTokens,
            int completionTokens,
            int totalTokens,
            List<SpanEntry> spans
    ) {
        TraceEntry withSpan(SpanEntry span, String now) {
            List<SpanEntry> newSpans = new ArrayList<>(spans);
            newSpans.add(span.withIndex(newSpans.size() + 1));
            return new TraceEntry(
                    traceId, startedAt, now, promptPreview,
                    newSpans.size(),
                    promptTokens + span.promptTokens(),
                    completionTokens + span.completionTokens(),
                    totalTokens + span.totalTokens(),
                    List.copyOf(newSpans));
        }
    }
}