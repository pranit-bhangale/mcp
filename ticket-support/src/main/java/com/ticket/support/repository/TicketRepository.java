package com.ticket.support.repository;

import com.ticket.support.entity.Status;
import com.ticket.support.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, UUID>, JpaSpecificationExecutor<Ticket> {

    @Query("""
                SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END
                FROM Ticket t
                WHERE LOWER(t.summary) = LOWER(:summary)
                  AND t.userId = :userId
                  AND t.username = :username
            """)
    boolean existsBySummaryAndUsernameAndUserId(
            @Param("summary") String summary,
            @Param("username") String username,
            @Param("userId") UUID userId
    );

    Optional<Ticket> findByTicketId(String ticketId);

    @Query("""
            SELECT t.status
            FROM Ticket t
            WHERE t.ticketId = :ticketId
            """)
    Status findStatusOfTicket(@Param("ticketId") String ticketId);
}
