package com.syncdesk.department.application.service;

import com.syncdesk.department.domain.Department;
import com.syncdesk.department.domain.DepartmentRepository;
import com.syncdesk.department.presentation.request.CreateDepartmentRequest;
import com.syncdesk.department.presentation.response.DepartmentResponse;
import com.syncdesk.shared.exception.BusinessException;
import com.syncdesk.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private static final Logger log = LoggerFactory.getLogger(DepartmentService.class);

    private final DepartmentRepository departmentRepository;

    @Transactional(readOnly = true)
    public List<DepartmentResponse> findAll() {
        log.debug("Listing all departments");
        return departmentRepository.findAll()
                .stream()
                .map(DepartmentResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public DepartmentResponse findById(UUID id) {
        log.debug("Finding department by id={}", id);
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("Department", id));
        return DepartmentResponse.from(department);
    }

    @Transactional
    public DepartmentResponse create(CreateDepartmentRequest request) {
        log.info("Creating department: name={}", request.name());
        if (departmentRepository.existsByName(request.name())) {
            log.warn("Department creation failed — already exists: {}", request.name());
            throw new BusinessException("Department already exists: " + request.name());
        }
        Department department = new Department(request.name());
        DepartmentResponse response = DepartmentResponse.from(departmentRepository.save(department));
        log.info("Department created: id={}, name={}", response.id(), response.name());
        return response;
    }

    @Transactional
    public DepartmentResponse rename(UUID id, CreateDepartmentRequest request) {
        log.info("Renaming department id={} to '{}'", id, request.name());
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("Department", id));
        department.rename(request.name());
        return DepartmentResponse.from(departmentRepository.save(department));
    }

    @Transactional
    public void delete(UUID id) {
        log.info("Deleting department id={}", id);
        departmentRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("Department", id));
        departmentRepository.deleteById(id);
        log.info("Department deleted: id={}", id);
    }
}
