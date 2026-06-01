package com.syncdesk.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class Password {

    @Column(name = "password", nullable = false)
    private String value;

    protected Password() {}

    public Password(String hashedValue) {
        if (hashedValue == null || hashedValue.isBlank()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        this.value = hashedValue;
    }

    public String getValue() {
        return value;
    }
}
