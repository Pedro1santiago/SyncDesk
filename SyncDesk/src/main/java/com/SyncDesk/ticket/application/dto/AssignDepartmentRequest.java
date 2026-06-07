package com.syncdesk.ticket.application.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AssignDepartmentRequest(@NotNull UUID departmentId) {}
