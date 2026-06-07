package com.syncdesk.ticket.domain.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class Title {

    @Column(name = "title", nullable = false)
    private String value;

    protected Title() {}

    public Title(String value) {
        validate(value);
        this.value = value;
    }

    private void validate(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
        if (value.length() > 100) {
            throw new IllegalArgumentException("Title is too long (max 100 chars)");
        }
    }

    public String getValue() {
        return value;
    }
}
