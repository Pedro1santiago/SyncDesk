CREATE TABLE departments (
    id   UUID         NOT NULL,
    name VARCHAR(100) NOT NULL,
    CONSTRAINT pk_departments PRIMARY KEY (id),
    CONSTRAINT uq_departments_name UNIQUE (name)
);
