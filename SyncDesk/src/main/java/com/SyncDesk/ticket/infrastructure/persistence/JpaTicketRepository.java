package com.syncdesk.ticket.infrastructure.persistence;

import com.syncdesk.ticket.domain.entity.Ticket;
import com.syncdesk.ticket.domain.enums.TicketStatus;
import com.syncdesk.ticket.domain.repository.TicketRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface JpaTicketRepository extends JpaRepository<Ticket, UUID>, TicketRepository {

    Page<Ticket> findByUserId(UUID userId, Pageable pageable);

    Page<Ticket> findByAssignedUserId(UUID userId, Pageable pageable);

    Page<Ticket> findByStatus(TicketStatus status, Pageable pageable);
}
