package com.syncdesk.ticket.application.dto;

import com.syncdesk.ticket.domain.entity.TicketMessage;
import com.syncdesk.user.presentation.response.UserResponse;

import java.time.LocalDateTime;
import java.util.UUID;

public record TicketMessageResponse(
        UUID id,
        String message,
        UserResponse user,
        LocalDateTime createdAt
) {
    public static TicketMessageResponse from(TicketMessage ticketMessage) {
        return new TicketMessageResponse(
                ticketMessage.getId(),
                ticketMessage.getMessage(),
                UserResponse.from(ticketMessage.getUser()),
                ticketMessage.getCreatedAt()
        );
    }
}
