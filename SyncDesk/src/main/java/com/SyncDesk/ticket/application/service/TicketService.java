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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketService {

    private static final Logger log = LoggerFactory.getLogger(TicketService.class);

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;

    @Transactional
    public TicketResponse create(CreateTicketRequest request, UserPrincipal principal) {
        log.info("Creating ticket: userId={}, priority={}, title='{}'", principal.getUserId(), request.priority(), request.title());
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
        log.debug("Listing tickets: userId={}, role={}", principal.getUserId(), principal.getUser().getRole());
        User user = principal.getUser();

        if (user.isSuperAdmin()) {
            return ticketRepository.findAll(pageable).map(TicketResponse::from);
        }
        if (user.isAdmin()) {
            if (user.getDepartment() == null) {
                return Page.empty(pageable);
            }
            return ticketRepository.findByDepartmentId(user.getDepartment().getId(), pageable).map(TicketResponse::from);
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
        log.info("Assigning ticket id={} to agentId={}", ticketId, request.agentId());
        rejectIfNotAdminOrAgent(principal);
        Ticket ticket = loadTicket(ticketId);
        validateAccess(ticket, principal);
        User agent = userRepository.findById(request.agentId())
                .orElseThrow(() -> NotFoundException.of("User", request.agentId()));
        if (!agent.isAgent() && !agent.isAdminOrAbove()) {
            throw new BusinessException("Assigned user must be an agent");
        }
        User requester = principal.getUser();
        if (requester.isAdmin() && !isSameDepartment(requester, agent)) {
            throw new BusinessException("Admin can only assign agents from their own department");
        }
        ticket.assignTo(agent);
        return TicketResponse.from(ticketRepository.save(ticket));
    }

    @Transactional
    public TicketResponse changeStatus(UUID ticketId, ChangeStatusRequest request, UserPrincipal principal) {
        log.info("Changing status of ticket id={} to '{}'", ticketId, request.status());
        rejectIfNotAdminOrAgent(principal);
        Ticket ticket = loadTicket(ticketId);
        validateAccess(ticket, principal);
        TicketStatus status = TicketStatus.valueOf(request.status().toUpperCase());
        ticket.changeStatus(status);
        return TicketResponse.from(ticketRepository.save(ticket));
    }

    @Transactional
    public TicketResponse changePriority(UUID ticketId, ChangePriorityRequest request, UserPrincipal principal) {
        log.info("Changing priority of ticket id={} to '{}'", ticketId, request.priority());
        rejectIfNotAdminOrAgent(principal);
        Ticket ticket = loadTicket(ticketId);
        validateAccess(ticket, principal);
        TicketPriority priority = TicketPriority.valueOf(request.priority().toUpperCase());
        ticket.changePriority(priority);
        return TicketResponse.from(ticketRepository.save(ticket));
    }

    @Transactional
    public TicketResponse assignDepartment(UUID ticketId, AssignDepartmentRequest request, UserPrincipal principal) {
        log.info("Assigning department id={} to ticket id={}", request.departmentId(), ticketId);
        rejectIfNotAdminOrAgent(principal);
        Ticket ticket = loadTicket(ticketId);
        validateAccess(ticket, principal);
        Department department = departmentRepository.findById(request.departmentId())
                .orElseThrow(() -> NotFoundException.of("Department", request.departmentId()));
        ticket.assignDepartment(department);
        return TicketResponse.from(ticketRepository.save(ticket));
    }

    @Transactional
    public TicketResponse close(UUID ticketId, UserPrincipal principal) {
        log.info("Closing ticket id={} by userId={}", ticketId, principal.getUserId());
        rejectIfNotAdminOrAgent(principal);
        Ticket ticket = loadTicket(ticketId);
        validateAccess(ticket, principal);
        ticket.close();
        return TicketResponse.from(ticketRepository.save(ticket));
    }

    @Transactional
    public TicketMessageResponse sendMessage(UUID ticketId, SendMessageRequest request, UserPrincipal principal) {
        log.info("Sending message on ticket id={} by userId={}", ticketId, principal.getUserId());
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
        log.info("Adding attachment '{}' to ticket id={} by userId={}", request.fileName(), ticketId, principal.getUserId());
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
        if (user.isSuperAdmin()) return;
        if (ticket.belongsTo(principal.getUserId())) return;
        if (ticket.isAssignedTo(principal.getUserId())) return;
        if (user.isAdmin() && ticket.isInDepartment(user.getDepartment())) return;
        throw new BusinessException("Access denied to this ticket");
    }

    private void rejectIfNotAdminOrAgent(UserPrincipal principal) {
        User user = principal.getUser();
        if (!user.isAdminOrAbove() && !user.isAgent()) {
            throw new BusinessException("Only agents and admins can perform this action");
        }
    }

    private boolean isSameDepartment(User admin, User agent) {
        if (admin.getDepartment() == null || agent.getDepartment() == null) return false;
        return admin.getDepartment().getId().equals(agent.getDepartment().getId());
    }
}
