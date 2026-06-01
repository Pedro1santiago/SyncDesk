package com.syncdesk.ticket.application.service;

import com.syncdesk.department.domain.Department;
import com.syncdesk.department.domain.DepartmentRepository;
import com.syncdesk.shared.exception.BusinessException;
import com.syncdesk.shared.exception.NotFoundException;
import com.syncdesk.shared.security.UserPrincipal;
import com.syncdesk.ticket.application.dto.*;
import com.syncdesk.ticket.domain.entity.Ticket;
import com.syncdesk.ticket.domain.enums.TicketPriority;
import com.syncdesk.ticket.domain.enums.TicketStatus;
import com.syncdesk.ticket.domain.repository.TicketRepository;
import com.syncdesk.ticket.domain.valueobject.Title;
import com.syncdesk.user.domain.User;
import com.syncdesk.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;

    @Transactional
    public TicketResponse create(CreateTicketRequest request, UserPrincipal principal) {
        User user = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> NotFoundException.of("User", principal.getUserId()));

        TicketPriority priority = TicketPriority.valueOf(request.priority().toUpperCase());

        Ticket ticket = new Ticket(user, new Title(request.title()), request.description(), priority);

        if (request.departmentId() != null) {
            Department department = departmentRepository.findById(request.departmentId())
                    .orElseThrow(() -> NotFoundException.of("Department", request.departmentId()));
            ticket.assignDepartment(department);
        }

        return TicketResponse.from(ticketRepository.save(ticket));
    }

    @Transactional(readOnly = true)
    public Page<TicketResponse> findAll(UserPrincipal principal, Pageable pageable) {
        User user = principal.getUser();

        if (user.isAdmin()) {
            return ticketRepository.findAll(pageable).map(TicketResponse::from);
        }
        if (user.isAgent()) {
            return ticketRepository.findByAssignedUserId(principal.getUserId(), pageable).map(TicketResponse::from);
        }
        return ticketRepository.findByUserId(principal.getUserId(), pageable).map(TicketResponse::from);
    }

    @Transactional(readOnly = true)
    public TicketResponse findById(UUID ticketId, UserPrincipal principal) {
        Ticket ticket = loadTicket(ticketId);
        validateAccess(ticket, principal);
        return TicketResponse.from(ticket);
    }

    @Transactional
    public TicketResponse assign(UUID ticketId, AssignTicketRequest request, UserPrincipal principal) {
        rejectIfNotAdminOrAgent(principal);
        Ticket ticket = loadTicket(ticketId);
        User agent = userRepository.findById(request.agentId())
                .orElseThrow(() -> NotFoundException.of("User", request.agentId()));
        ticket.assignTo(agent);
        return TicketResponse.from(ticketRepository.save(ticket));
    }

    @Transactional
    public TicketResponse changeStatus(UUID ticketId, ChangeStatusRequest request, UserPrincipal principal) {
        rejectIfNotAdminOrAgent(principal);
        Ticket ticket = loadTicket(ticketId);
        TicketStatus status = TicketStatus.valueOf(request.status().toUpperCase());
        ticket.changeStatus(status);
        return TicketResponse.from(ticketRepository.save(ticket));
    }

    @Transactional
    public TicketResponse changePriority(UUID ticketId, ChangePriorityRequest request, UserPrincipal principal) {
        rejectIfNotAdminOrAgent(principal);
        Ticket ticket = loadTicket(ticketId);
        TicketPriority priority = TicketPriority.valueOf(request.priority().toUpperCase());
        ticket.changePriority(priority);
        return TicketResponse.from(ticketRepository.save(ticket));
    }

    @Transactional
    public TicketResponse assignDepartment(UUID ticketId, AssignDepartmentRequest request, UserPrincipal principal) {
        rejectIfNotAdminOrAgent(principal);
        Ticket ticket = loadTicket(ticketId);
        Department department = departmentRepository.findById(request.departmentId())
                .orElseThrow(() -> NotFoundException.of("Department", request.departmentId()));
        ticket.assignDepartment(department);
        return TicketResponse.from(ticketRepository.save(ticket));
    }

    @Transactional
    public TicketResponse close(UUID ticketId, UserPrincipal principal) {
        rejectIfNotAdminOrAgent(principal);
        Ticket ticket = loadTicket(ticketId);
        ticket.close();
        return TicketResponse.from(ticketRepository.save(ticket));
    }

    @Transactional
    public TicketMessageResponse sendMessage(UUID ticketId, SendMessageRequest request, UserPrincipal principal) {
        Ticket ticket = loadTicket(ticketId);
        validateAccess(ticket, principal);

        User sender = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> NotFoundException.of("User", principal.getUserId()));

        ticket.addMessage(sender, request.message());
        Ticket saved = ticketRepository.save(ticket);

        return saved.getMessages().stream()
                .filter(m -> m.getMessage().equals(request.message()))
                .findFirst()
                .map(TicketMessageResponse::from)
                .orElseThrow();
    }

    @Transactional(readOnly = true)
    public List<TicketMessageResponse> getMessages(UUID ticketId, UserPrincipal principal) {
        Ticket ticket = loadTicket(ticketId);
        validateAccess(ticket, principal);
        return ticket.getMessages().stream()
                .map(TicketMessageResponse::from)
                .toList();
    }

    @Transactional
    public TicketAttachmentResponse addAttachment(UUID ticketId, AddAttachmentRequest request, UserPrincipal principal) {
        Ticket ticket = loadTicket(ticketId);
        validateAccess(ticket, principal);
        ticket.addAttachment(request.fileName(), request.fileUrl());
        Ticket saved = ticketRepository.save(ticket);
        return saved.getAttachments().stream()
                .filter(a -> a.getFileUrl().equals(request.fileUrl()))
                .findFirst()
                .map(TicketAttachmentResponse::from)
                .orElseThrow();
    }

    @Transactional(readOnly = true)
    public List<TicketAttachmentResponse> getAttachments(UUID ticketId, UserPrincipal principal) {
        Ticket ticket = loadTicket(ticketId);
        validateAccess(ticket, principal);
        return ticket.getAttachments().stream()
                .map(TicketAttachmentResponse::from)
                .toList();
    }

    private Ticket loadTicket(UUID ticketId) {
        return ticketRepository.findById(ticketId)
                .orElseThrow(() -> NotFoundException.of("Ticket", ticketId));
    }

    private void validateAccess(Ticket ticket, UserPrincipal principal) {
        User user = principal.getUser();
        boolean hasAccess = user.isAdmin()
                || ticket.belongsTo(principal.getUserId())
                || ticket.isAssignedTo(principal.getUserId());
        if (!hasAccess) {
            throw new BusinessException("Access denied to this ticket");
        }
    }

    private void rejectIfNotAdminOrAgent(UserPrincipal principal) {
        User user = principal.getUser();
        if (!user.isAdmin() && !user.isAgent()) {
            throw new BusinessException("Only agents and admins can perform this action");
        }
    }
}
