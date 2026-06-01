package com.syncdesk.ticket.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateTicketRequest(
        @NotBlank @Size(min = 5, max = 100) String title,
        @NotBlank @Size(min = 10) String description,
        @NotNull String priority,
        UUID departmentId
) {}
