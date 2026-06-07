package com.syncdesk.user.domain;

import com.syncdesk.department.domain.Department;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "users")
public class User {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @Embedded
    private Email email;

    @Embedded
    private Password password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public User(Department department, String username, Email email, Password password, Role role) {
        this.id = UUID.randomUUID();
        this.department = department;
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public void changeRole(Role newRole) {
        this.role = newRole;
    }

    public void changeDepartment(Department newDepartment) {
        this.department = newDepartment;
    }

    public void changePassword(Password newPassword) {
        this.password = newPassword;
    }

    public boolean isAdmin() {
        return Role.ADMIN.equals(this.role);
    }

    public boolean isSuperAdmin() {
        return Role.SUPER_ADMIN.equals(this.role);
    }

    public boolean isAdminOrAbove() {
        return isAdmin() || isSuperAdmin();
    }

    public boolean isAgent() {
        return Role.AGENT.equals(this.role);
    }
}
