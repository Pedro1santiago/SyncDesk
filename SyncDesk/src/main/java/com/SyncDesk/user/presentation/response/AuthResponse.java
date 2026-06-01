package com.syncdesk.user.presentation.response;

public record AuthResponse(String token, String email, String role) {}
