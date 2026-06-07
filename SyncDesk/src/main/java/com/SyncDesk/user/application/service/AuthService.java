package com.syncdesk.user.application.service;

import com.syncdesk.shared.exception.BusinessException;
import com.syncdesk.shared.security.JwtService;
import com.syncdesk.shared.security.UserPrincipal;
import com.syncdesk.user.domain.Email;
import com.syncdesk.user.domain.Password;
import com.syncdesk.user.domain.Role;
import com.syncdesk.user.domain.User;
import com.syncdesk.user.domain.UserRepository;
import com.syncdesk.user.presentation.request.LoginRequest;
import com.syncdesk.user.presentation.request.RegisterRequest;
import com.syncdesk.user.presentation.response.AuthResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering user: email={}, username={}", request.email(), request.username());
        if (userRepository.existsByEmail(request.email())) {
            log.warn("Registration failed — email already in use: {}", request.email());
            throw new BusinessException("Email already in use: " + request.email());
        }
        if (userRepository.existsByUsername(request.username())) {
            log.warn("Registration failed — username already taken: {}", request.username());
            throw new BusinessException("Username already taken: " + request.username());
        }

        User user = new User(
                null,
                request.username(),
                new Email(request.email()),
                new Password(passwordEncoder.encode(request.password())),
                Role.USER
        );

        userRepository.save(user);
        log.info("User registered successfully: id={}, email={}", user.getId(), user.getEmail().getValue());

        UserPrincipal principal = new UserPrincipal(user);
        String token = jwtService.generateToken(principal);

        return new AuthResponse(token, user.getEmail().getValue(), user.getRole().name());
    }

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
