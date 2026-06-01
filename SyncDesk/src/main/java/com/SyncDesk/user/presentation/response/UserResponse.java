package com.syncdesk.user.presentation.response;

import com.syncdesk.user.domain.User;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String username,
        String email,
        String role,
        String department,
        LocalDateTime createdAt
) {
    public static UserResponse from(User user) {
        String departmentName = user.getDepartment() != null ? user.getDepartment().getName() : null;
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail().getValue(),
                user.getRole().name(),
                departmentName,
                user.getCreatedAt()
        );
    }
}
