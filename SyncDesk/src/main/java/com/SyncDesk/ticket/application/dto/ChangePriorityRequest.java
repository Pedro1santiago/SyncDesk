package com.syncdesk.ticket.application.dto;

import jakarta.validation.constraints.NotBlank;

public record ChangePriorityRequest(@NotBlank String priority) {}
