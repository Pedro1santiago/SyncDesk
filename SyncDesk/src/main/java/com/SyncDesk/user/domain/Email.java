package com.syncdesk.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class Email {

    @Column(name = "email", unique = true, nullable = false)
    private String value;

    protected Email() {}

    public Email(String value) {
        this.value = validate(value);
    }

    private String validate(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            throw new IllegalArgumentException("Email obrigatório");
        }

        int arroba = rawValue.indexOf("@");
        int ponto = rawValue.lastIndexOf(".");

        boolean valido = arroba > 0
                && ponto > arroba + 1
                && ponto < rawValue.length() - 1;

        if (!valido) {
            throw new IllegalArgumentException("Email inválido: " + rawValue);
        }

        return rawValue.toLowerCase();
    }

    public String getValue() {
        return value;
    }
}
