package com.syncdesk.ticket.domain.entity;

import com.syncdesk.department.domain.Department;
import com.syncdesk.ticket.domain.valueobject.TicketDepartmentId;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ticket_departments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TicketDepartment {

    @EmbeddedId
    private TicketDepartmentId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("ticketId")
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("departmentId")
    @JoinColumn(name = "department_id")
    private Department department;

    public TicketDepartment(Ticket ticket, Department department) {
        this.ticket = ticket;
        this.department = department;
        this.id = new TicketDepartmentId(
                ticket.getId(),
                department.getId()
        );
    }
}
