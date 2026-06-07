package com.syncdesk.user.application.service;

import com.syncdesk.department.domain.Department;
import com.syncdesk.department.domain.DepartmentRepository;
import com.syncdesk.shared.exception.BusinessException;
import com.syncdesk.shared.exception.NotFoundException;
import com.syncdesk.shared.security.UserPrincipal;
import com.syncdesk.user.domain.*;
import com.syncdesk.user.presentation.request.CreateUserRequest;
import com.syncdesk.user.presentation.request.UpdateUserRequest;
import com.syncdesk.user.presentation.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public Page<UserResponse> findAll(UserPrincipal principal, Pageable pageable) {
        log.debug("Listing users: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        User requester = principal.getUser();
        if (requester.isAdmin()) {
            if (requester.getDepartment() == null) return Page.empty(pageable);
            return userRepository.findByDepartmentId(requester.getDepartment().getId(), pageable).map(UserResponse::from);
        }
        return userRepository.findAll(pageable).map(UserResponse::from);
    }

    @Transactional(readOnly = true)
    public UserResponse findById(UUID id, UserPrincipal principal) {
        log.debug("Finding user by id={}", id);
        User requester = principal.getUser();
        User user = userRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("User", id));
        if (requester.isAdmin() && !isSameDepartment(requester, user)) {
            throw new BusinessException("Access denied to this user");
        }
        return UserResponse.from(user);
    }

    @Transactional(readOnly = true)
    public UserResponse me(UserPrincipal principal) {
        User user = userRepository.findById(principal.getUserId())
                .orElseThrow(() -> NotFoundException.of("User", principal.getUserId()));
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse create(CreateUserRequest request, UserPrincipal principal) {
        log.info("Creating user: email={}, username={}, role={}", request.email(), request.username(), request.role());
        User requester = principal.getUser();
        if (userRepository.existsByEmail(request.email())) {
            log.warn("User creation failed — email already in use: {}", request.email());
            throw new BusinessException("Email already in use: " + request.email());
        }
        if (userRepository.existsByUsername(request.username())) {
            log.warn("User creation failed — username already taken: {}", request.username());
            throw new BusinessException("Username already taken: " + request.username());
        }

        Role role = Role.valueOf(request.role().toUpperCase());
        Department department = resolveOptionalDepartment(request.departmentId());

        if (requester.isAdmin()) {
            // ADMIN can only create USER or AGENT, and only within their own department
            if (role == Role.ADMIN || role == Role.SUPER_ADMIN) {
                throw new BusinessException("Admin can only create USER or AGENT accounts");
            }
            if (requester.getDepartment() == null) {
                throw new BusinessException("Admin must belong to a department to create users");
            }
            department = requester.getDepartment();
        } else {
            // SUPER_ADMIN: ADMIN role requires a department
            if (role == Role.ADMIN && department == null) {
                throw new BusinessException("A department is required when creating an ADMIN user");
            }
        }

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
    public UserResponse update(UUID id, UpdateUserRequest request, UserPrincipal principal) {
        log.info("Updating user id={}: role={}, departmentId={}", id, request.role(), request.departmentId());
        User requester = principal.getUser();
        User user = userRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("User", id));

        if (requester.isAdmin() && !isSameDepartment(requester, user)) {
            throw new BusinessException("Access denied to this user");
        }

        if (request.role() != null) {
            Role newRole = Role.valueOf(request.role().toUpperCase());
            if (requester.isAdmin() && (newRole == Role.ADMIN || newRole == Role.SUPER_ADMIN)) {
                throw new BusinessException("Admin cannot assign ADMIN or SUPER_ADMIN roles");
            }
            user.changeRole(newRole);
        }

        if (request.departmentId() != null) {
            if (requester.isAdmin()) {
                throw new BusinessException("Admin cannot change a user's department");
            }
            Department department = departmentRepository.findById(request.departmentId())
                    .orElseThrow(() -> NotFoundException.of("Department", request.departmentId()));
            user.changeDepartment(department);
        }

        if (user.isAdmin() && user.getDepartment() == null) {
            throw new BusinessException("A department is required for ADMIN users");
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

    private boolean isSameDepartment(User requester, User target) {
        if (requester.getDepartment() == null || target.getDepartment() == null) return false;
        return requester.getDepartment().getId().equals(target.getDepartment().getId());
    }
}
