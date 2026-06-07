package com.syncdesk.ticket.domain.repository;

import com.syncdesk.ticket.domain.entity.Ticket;
import com.syncdesk.ticket.domain.enums.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface TicketRepository {

    Ticket save(Ticket ticket);

    Optional<Ticket> findById(UUID id);

    Page<Ticket> findAll(Pageable pageable);

    Page<Ticket> findByUserId(UUID userId, Pageable pageable);

    Page<Ticket> findByAssignedUserId(UUID userId, Pageable pageable);

    Page<Ticket> findByStatus(TicketStatus status, Pageable pageable);

    Page<Ticket> findByDepartmentId(UUID departmentId, Pageable pageable);
}
