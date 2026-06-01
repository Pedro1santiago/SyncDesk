package com.syncdesk.department.presentation.response;

import com.syncdesk.department.domain.Department;

import java.util.UUID;

public record DepartmentResponse(UUID id, String name) {

    public static DepartmentResponse from(Department department) {
        return new DepartmentResponse(department.getId(), department.getName());
    }
}
