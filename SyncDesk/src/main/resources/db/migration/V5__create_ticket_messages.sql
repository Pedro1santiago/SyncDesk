CREATE TABLE ticket_messages (
    id         UUID      NOT NULL,
    ticket_id  UUID      NOT NULL,
    user_id    UUID      NOT NULL,
    message    TEXT      NOT NULL,
    created_at TIMESTAMP,
    CONSTRAINT pk_ticket_messages PRIMARY KEY (id),
    CONSTRAINT fk_tm_ticket FOREIGN KEY (ticket_id) REFERENCES tickets (id),
    CONSTRAINT fk_tm_user FOREIGN KEY (user_id) REFERENCES users (id)
);
