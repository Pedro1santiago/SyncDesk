package com.syncdesk.ticket.domain.valueobject;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Embeddable
@NoArgsConstructor
public class TicketDepartmentId implements Serializable {

    private UUID ticketId;
    private UUID departmentId;

    public TicketDepartmentId(UUID ticketId, UUID departmentId) {
        this.ticketId = ticketId;
        this.departmentId = departmentId;
    }
}
