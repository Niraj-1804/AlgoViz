package com.algoviz.service;

import com.algoviz.model.AppUser;
import com.algoviz.repository.AppUserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public AppUser register(String email, String password) {
        if (email == null || email.trim().isEmpty()
                || password == null || password.length() < 8) {
            throw new IllegalArgumentException(
                    "Email is required and password must be at least 8 characters"
            );
        }

        String normalizedEmail = email.trim().toLowerCase();

        if (appUserRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException(
                    "An account with this email already exists"
            );
        }

        String hashedPassword = passwordEncoder.encode(password);

        AppUser user = new AppUser(normalizedEmail, hashedPassword);

        return appUserRepository.save(user);
    }

    public boolean validateLogin(String email, String password) {
        if (email == null || password == null) {
            return false;
        }

        return appUserRepository.findByEmail(
                email.trim().toLowerCase()
        ).map(user -> passwordEncoder.matches(
                password, user.getPassword()
        )).orElse(false);
    }
}
