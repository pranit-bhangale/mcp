CREATE TABLE IF NOT EXISTS tickets (
    id          UUID         NOT NULL,
    ticket_id   VARCHAR(50)  NOT NULL,
    user_id     UUID         NOT NULL,
    username    VARCHAR(100) NOT NULL,
    summary     VARCHAR(100) NOT NULL,
    description TEXT         NOT NULL,
    priority    VARCHAR(20)  NOT NULL DEFAULT 'MEDIUM',
    category    VARCHAR(30)  NOT NULL DEFAULT 'NOT_DEFINED',
    status      VARCHAR(30)  NOT NULL DEFAULT 'PENDING',
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version     BIGINT       NOT NULL DEFAULT 0,

    CONSTRAINT pk_tickets
    PRIMARY KEY (id),

    CONSTRAINT uk_ticket_ticket_id
    UNIQUE (ticket_id),

    CONSTRAINT uk_ticket_user_summary
    UNIQUE (user_id, summary),

    CONSTRAINT chk_ticket_priority
    CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),

    CONSTRAINT chk_ticket_category
    CHECK (category IN ('TECHNICAL', 'BANKING', 'NOT_DEFINED')),

    CONSTRAINT chk_ticket_status
    CHECK (status IN ('PENDING', 'IN_PROGRESS', 'RESOLVED', 'CLOSED'))
    );

CREATE INDEX idx_ticket_user_id
    ON tickets (user_id);

CREATE INDEX idx_ticket_username
    ON tickets (username);

CREATE INDEX idx_ticket_status
    ON tickets (status);

CREATE INDEX idx_ticket_priority
    ON tickets (priority);

CREATE INDEX idx_ticket_category
    ON tickets (category);

CREATE INDEX idx_ticket_created_at
    ON tickets (created_at DESC);

CREATE INDEX idx_ticket_status_priority
    ON tickets (status, priority);

CREATE INDEX idx_ticket_user_created
    ON tickets (user_id, created_at DESC);