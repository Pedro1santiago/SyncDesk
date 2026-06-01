package com.syncdesk.department.application.service;

import com.syncdesk.department.domain.Department;
import com.syncdesk.department.domain.DepartmentRepository;
import com.syncdesk.department.presentation.request.CreateDepartmentRequest;
import com.syncdesk.department.presentation.response.DepartmentResponse;
import com.syncdesk.shared.exception.BusinessException;
import com.syncdesk.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    @Transactional(readOnly = true)
    public List<DepartmentResponse> findAll() {
        return departmentRepository.findAll()
                .stream()
                .map(DepartmentResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public DepartmentResponse findById(UUID id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("Department", id));
        return DepartmentResponse.from(department);
    }

    @Transactional
    public DepartmentResponse create(CreateDepartmentRequest request) {
        if (departmentRepository.existsByName(request.name())) {
            throw new BusinessException("Department already exists: " + request.name());
        }
        Department department = new Department(request.name());
        return DepartmentResponse.from(departmentRepository.save(department));
    }

    @Transactional
    public DepartmentResponse rename(UUID id, CreateDepartmentRequest request) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("Department", id));
        department.rename(request.name());
        return DepartmentResponse.from(departmentRepository.save(department));
    }

    @Transactional
    public void delete(UUID id) {
        if (!departmentRepository.findById(id).isPresent()) {
            throw NotFoundException.of("Department", id);
        }
        departmentRepository.deleteById(id);
    }
}
