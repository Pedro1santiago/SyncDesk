package com.syncdesk.department.infrastructure.persistence;

import com.syncdesk.department.domain.Department;
import com.syncdesk.department.domain.DepartmentRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface JpaDepartmentRepository extends JpaRepository<Department, UUID>, DepartmentRepository {

    boolean existsByName(String name);
}
