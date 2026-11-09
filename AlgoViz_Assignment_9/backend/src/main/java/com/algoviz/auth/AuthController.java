package com.algoviz.auth;

import com.algoviz.security.JwtService;
import com.algoviz.user.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api")
public class AuthController {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    public AuthController(UserRepository users,PasswordEncoder encoder,JwtService jwt) {
        this.users=users; this.encoder=encoder; this.jwt=jwt;
    }
    public record LoginRequest(@NotBlank String username,@NotBlank String password) {}
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        var match=users.findByUsername(request.username());
        if (match.isEmpty() || !encoder.matches(request.password(), match.get().getPasswordHash()))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error","invalid credentials"));
        var user=match.get();
        return ResponseEntity.ok(Map.of("token",jwt.issue(user),"role",user.getRole().name(),"username",user.getUsername()));
    }
}
