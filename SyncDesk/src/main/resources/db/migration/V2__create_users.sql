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
