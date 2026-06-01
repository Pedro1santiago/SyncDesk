package com.syncdesk.department.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "departments")
public class Department {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    public Department(String name) {
        this.id = UUID.randomUUID();
        this.name = name;
    }

    public void rename(String newName) {
        if (newName == null || newName.isBlank()) {
            throw new IllegalArgumentException("Department name cannot be empty");
        }
        this.name = newName;
    }
}
