package com.syncdesk.department.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DepartmentRepository {

    Department save(Department department);

    Optional<Department> findById(UUID id);

    List<Department> findAll();

    boolean existsByName(String name);

    void deleteById(UUID id);
}
