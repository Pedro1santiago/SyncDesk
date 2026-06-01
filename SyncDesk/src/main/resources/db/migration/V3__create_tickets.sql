CREATE TABLE tickets (
    id               UUID         NOT NULL,
    user_id          UUID         NOT NULL,
    assigned_user_id UUID,
    title            VARCHAR(100) NOT NULL,
    description      TEXT         NOT NULL,
    status           VARCHAR(30)  NOT NULL,
    priority         VARCHAR(20)  NOT NULL,
    created_at       TIMESTAMP,
    updated_at       TIMESTAMP,
    closed_at        TIMESTAMP,
    CONSTRAINT pk_tickets PRIMARY KEY (id),
    CONSTRAINT fk_tickets_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_tickets_assigned_user FOREIGN KEY (assigned_user_id) REFERENCES users (id)
);
