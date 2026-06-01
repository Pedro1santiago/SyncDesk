package com.syncdesk.user.application.service;

import com.syncdesk.department.domain.Department;
import com.syncdesk.department.domain.DepartmentRepository;
import com.syncdesk.shared.exception.BusinessException;
import com.syncdesk.shared.exception.NotFoundException;
import com.syncdesk.user.domain.*;
import com.syncdesk.user.presentation.request.CreateUserRequest;
import com.syncdesk.user.presentation.request.UpdateUserRequest;
import com.syncdesk.user.presentation.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public Page<UserResponse> findAll(Pageable pageable) {
        return userRepository.findAll(pageable).map(UserResponse::from);
    }

    @Transactional(readOnly = true)
    public UserResponse findById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("User", id));
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse create(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException("Email already in use: " + request.email());
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new BusinessException("Username already taken: " + request.username());
        }

        Department department = resolveOptionalDepartment(request.departmentId());
        Role role = Role.valueOf(request.role().toUpperCase());

        User user = new User(
                department,
                request.username(),
                new Email(request.email()),
                new Password(passwordEncoder.encode(request.password())),
                role
        );

        return UserResponse.from(userRepository.save(user));
    }

    @Transactional
    public UserResponse update(UUID id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("User", id));

        if (request.role() != null) {
            user.changeRole(Role.valueOf(request.role().toUpperCase()));
        }

        if (request.departmentId() != null) {
            Department department = departmentRepository.findById(request.departmentId())
                    .orElseThrow(() -> NotFoundException.of("Department", request.departmentId()));
            user.changeDepartment(department);
        }

        return UserResponse.from(userRepository.save(user));
    }

    private Department resolveOptionalDepartment(UUID departmentId) {
        if (departmentId == null) {
            return null;
        }
        return departmentRepository.findById(departmentId)
                .orElseThrow(() -> NotFoundException.of("Department", departmentId));
    }
}
