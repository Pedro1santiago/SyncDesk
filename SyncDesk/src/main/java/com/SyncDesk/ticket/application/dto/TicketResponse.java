package com.syncdesk.ticket.application.dto;

import com.syncdesk.ticket.domain.entity.Ticket;
import com.syncdesk.user.presentation.response.UserResponse;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public record TicketResponse(
        UUID id,
        String title,
        String description,
        String status,
        String priority,
        UserResponse user,
        UserResponse assignedUser,
        Set<String> departments,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime closedAt
) {
    public static TicketResponse from(Ticket ticket) {
        Set<String> departments = ticket.getTicketDepartments().stream()
                .map(td -> td.getDepartment().getName())
                .collect(Collectors.toSet());

        UserResponse assignedUserResponse = ticket.getAssignedUser() != null
                ? UserResponse.from(ticket.getAssignedUser())
                : null;

        return new TicketResponse(
                ticket.getId(),
                ticket.getTitle().getValue(),
                ticket.getDescription(),
                ticket.getStatus().name(),
                ticket.getPriority().name(),
                UserResponse.from(ticket.getUser()),
                assignedUserResponse,
                departments,
                ticket.getCreatedAt(),
                ticket.getUpdatedAt(),
                ticket.getClosedAt()
        );
    }
}
