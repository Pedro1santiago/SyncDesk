CREATE TABLE ticket_departments (
    ticket_id     UUID NOT NULL,
    department_id UUID NOT NULL,
    CONSTRAINT pk_ticket_departments PRIMARY KEY (ticket_id, department_id),
    CONSTRAINT fk_td_ticket FOREIGN KEY (ticket_id) REFERENCES tickets (id),
    CONSTRAINT fk_td_department FOREIGN KEY (department_id) REFERENCES departments (id)
);
