package com.syncdesk.user.application.service;

import com.syncdesk.shared.security.JwtService;
import com.syncdesk.shared.security.UserPrincipal;
import com.syncdesk.user.presentation.request.LoginRequest;
import com.syncdesk.user.presentation.response.AuthResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt: email={}", request.email());
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
        String token = jwtService.generateToken(principal);
        log.info("Login successful: email={}, role={}", principal.getUsername(), principal.getUser().getRole());

        return new AuthResponse(token, principal.getUsername(), principal.getUser().getRole().name());
    }
}
