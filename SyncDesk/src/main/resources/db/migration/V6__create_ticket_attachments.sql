CREATE TABLE ticket_attachments (
    id          UUID         NOT NULL,
    ticket_id   UUID         NOT NULL,
    file_name   VARCHAR(255) NOT NULL,
    file_url    VARCHAR(500) NOT NULL,
    uploaded_at TIMESTAMP,
    CONSTRAINT pk_ticket_attachments PRIMARY KEY (id),
    CONSTRAINT fk_ta_ticket FOREIGN KEY (ticket_id) REFERENCES tickets (id)
);
