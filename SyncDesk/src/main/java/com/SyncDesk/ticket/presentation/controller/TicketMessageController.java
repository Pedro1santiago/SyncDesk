package com.syncdesk.ticket.presentation.controller;

import com.syncdesk.shared.security.UserPrincipal;
import com.syncdesk.ticket.application.dto.SendMessageRequest;
import com.syncdesk.ticket.application.dto.TicketMessageResponse;
import com.syncdesk.ticket.application.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tickets/{ticketId}/messages")
@RequiredArgsConstructor
public class TicketMessageController {

    private final TicketService ticketService;

    @GetMapping
    public ResponseEntity<List<TicketMessageResponse>> getMessages(
            @PathVariable UUID ticketId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(ticketService.getMessages(ticketId, principal));
    }

    @PostMapping
    public ResponseEntity<TicketMessageResponse> sendMessage(
            @PathVariable UUID ticketId,
            @Valid @RequestBody SendMessageRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ticketService.sendMessage(ticketId, request, principal));
    }
}
