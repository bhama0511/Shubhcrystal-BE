package com.shubhcrystals.service;

import com.shubhcrystals.dto.AuthResponse;
import com.shubhcrystals.dto.LoginRequest;
import com.shubhcrystals.dto.RegisterRequest;
import com.shubhcrystals.model.PasswordResetToken;
import com.shubhcrystals.model.Role;
import com.shubhcrystals.model.User;
import com.shubhcrystals.repository.PasswordResetTokenRepository;
import com.shubhcrystals.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AuthService.class);

    private static final long RESET_TOKEN_TTL_MINUTES = 30;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authManager;
    private final EmailService emailService;
    private final PasswordResetTokenRepository resetTokenRepository;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       JwtService jwtService, AuthenticationManager authManager,
                       EmailService emailService,
                       PasswordResetTokenRepository resetTokenRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authManager = authManager;
        this.emailService = emailService;
        this.resetTokenRepository = resetTokenRepository;
    }

    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }
        User user = new User();
        user.setName(req.getName());
        user.setEmail(req.getEmail());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setRole(Role.USER);
        userRepository.save(user);

        try {
            emailService.sendWelcome(user);
        } catch (Exception e) {
            log.warn("Welcome email enqueue failed for {}: {}", user.getEmail(), e.getMessage());
        }

        return new AuthResponse(jwtService.generateToken(user), user.getEmail(), user.getName(), user.getRole().name());
    }

    public AuthResponse login(LoginRequest req) {
        authManager.authenticate(
            new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
        );
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return new AuthResponse(jwtService.generateToken(user), user.getEmail(), user.getName(), user.getRole().name());
    }

    @Transactional
    public void requestPasswordReset(String email) {
        Optional<User> maybe = userRepository.findByEmail(email);
        if (maybe.isEmpty()) {
            log.info("Password reset requested for unknown email — silent skip");
            return;
        }
        User user = maybe.get();
        resetTokenRepository.deleteByUserId(user.getId());

        PasswordResetToken token = new PasswordResetToken();
        token.setUserId(user.getId());
        token.setToken(UUID.randomUUID().toString().replace("-", ""));
        token.setExpiresAt(Instant.now().plus(RESET_TOKEN_TTL_MINUTES, ChronoUnit.MINUTES));
        resetTokenRepository.save(token);

        try {
            emailService.sendPasswordReset(user, token.getToken());
        } catch (Exception e) {
            log.warn("Reset email enqueue failed for {}: {}", email, e.getMessage());
        }
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken prt = resetTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired reset link"));
        if (!prt.isValid()) {
            throw new IllegalArgumentException("Invalid or expired reset link");
        }
        User user = userRepository.findById(prt.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired reset link"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        prt.setUsed(true);
        resetTokenRepository.save(prt);
    }
}
