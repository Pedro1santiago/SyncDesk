package com.syncdesk.ticket.presentation.controller;

import com.syncdesk.shared.security.UserPrincipal;
import com.syncdesk.ticket.application.dto.AddAttachmentRequest;
import com.syncdesk.ticket.application.dto.TicketAttachmentResponse;
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
@RequestMapping("/api/tickets/{ticketId}/attachments")
@RequiredArgsConstructor
public class TicketAttachmentController {

    private final TicketService ticketService;

    @GetMapping
    public ResponseEntity<List<TicketAttachmentResponse>> getAttachments(
            @PathVariable UUID ticketId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(ticketService.getAttachments(ticketId, principal));
    }

    @PostMapping
    public ResponseEntity<TicketAttachmentResponse> addAttachment(
            @PathVariable UUID ticketId,
            @Valid @RequestBody AddAttachmentRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ticketService.addAttachment(ticketId, request, principal));
    }
}
