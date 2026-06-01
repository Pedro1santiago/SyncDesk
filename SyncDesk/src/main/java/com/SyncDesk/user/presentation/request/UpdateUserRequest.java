package com.syncdesk.user.presentation.request;

import java.util.UUID;

public record UpdateUserRequest(String role, UUID departmentId) {}
