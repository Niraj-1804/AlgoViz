package com.algoviz.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class UserSeeder implements CommandLineRunner {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final String studentPassword;
    private final String adminPassword;

    public UserSeeder(UserRepository users, PasswordEncoder encoder,
                      @Value("${app.seed.student-password}") String studentPassword,
                      @Value("${app.seed.admin-password}") String adminPassword) {
        this.users=users; this.encoder=encoder;
        this.studentPassword=studentPassword; this.adminPassword=adminPassword;
    }
    @Override public void run(String... args) {
        seed("student", studentPassword, Role.STUDENT);
        seed("admin", adminPassword, Role.ADMIN);
    }
    private void seed(String username, String raw, Role role) {
        if (raw.isBlank()) throw new IllegalStateException("Seed passwords must not be blank");
        if (users.findByUsername(username).isEmpty())
            users.save(new AppUser(username, encoder.encode(raw), role));
    }
}
