CREATE TABLE departments (
    id   UUID         NOT NULL,
    name VARCHAR(100) NOT NULL,
    CONSTRAINT pk_departments PRIMARY KEY (id),
    CONSTRAINT uq_departments_name UNIQUE (name)
);

CREATE TABLE users (
    id            UUID         NOT NULL,
    department_id UUID,
    username      VARCHAR(100) NOT NULL,
    email         VARCHAR(255) NOT NULL,
    password      VARCHAR(255) NOT NULL,
    role          VARCHAR(20)  NOT NULL,
    created_at    TIMESTAMP,
    updated_at    TIMESTAMP,
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT fk_users_department FOREIGN KEY (department_id) REFERENCES departments (id)
);

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

CREATE TABLE ticket_departments (
    ticket_id     UUID NOT NULL,
    department_id UUID NOT NULL,
    CONSTRAINT pk_ticket_departments PRIMARY KEY (ticket_id, department_id),
    CONSTRAINT fk_td_ticket FOREIGN KEY (ticket_id) REFERENCES tickets (id),
    CONSTRAINT fk_td_department FOREIGN KEY (department_id) REFERENCES departments (id)
);

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

CREATE TABLE ticket_attachments (
    id          UUID         NOT NULL,
    ticket_id   UUID         NOT NULL,
    file_name   VARCHAR(255) NOT NULL,
    file_url    VARCHAR(500) NOT NULL,
    uploaded_at TIMESTAMP,
    CONSTRAINT pk_ticket_attachments PRIMARY KEY (id),
    CONSTRAINT fk_ta_ticket FOREIGN KEY (ticket_id) REFERENCES tickets (id)
);
