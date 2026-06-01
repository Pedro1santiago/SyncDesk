package com.syncdesk.ticket.presentation.controller;

import com.syncdesk.shared.security.UserPrincipal;
import com.syncdesk.ticket.application.dto.*;
import com.syncdesk.ticket.application.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PostMapping
    public ResponseEntity<TicketResponse> create(
            @Valid @RequestBody CreateTicketRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ticketService.create(request, principal));
    }

    @GetMapping
    public ResponseEntity<Page<TicketResponse>> findAll(
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(ticketService.findAll(principal, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketResponse> findById(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(ticketService.findById(id, principal));
    }

    @PatchMapping("/{id}/assign")
    public ResponseEntity<TicketResponse> assign(
            @PathVariable UUID id,
            @Valid @RequestBody AssignTicketRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(ticketService.assign(id, request, principal));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TicketResponse> changeStatus(
            @PathVariable UUID id,
            @Valid @RequestBody ChangeStatusRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(ticketService.changeStatus(id, request, principal));
    }

    @PatchMapping("/{id}/priority")
    public ResponseEntity<TicketResponse> changePriority(
            @PathVariable UUID id,
            @Valid @RequestBody ChangePriorityRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(ticketService.changePriority(id, request, principal));
    }

    @PatchMapping("/{id}/department")
    public ResponseEntity<TicketResponse> assignDepartment(
            @PathVariable UUID id,
            @Valid @RequestBody AssignDepartmentRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(ticketService.assignDepartment(id, request, principal));
    }

    @PatchMapping("/{id}/close")
    public ResponseEntity<TicketResponse> close(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(ticketService.close(id, principal));
    }
}
