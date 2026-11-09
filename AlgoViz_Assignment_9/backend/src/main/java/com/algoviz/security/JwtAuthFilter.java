package com.algoviz.security;

import com.algoviz.user.AppUser;
import com.algoviz.user.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtService jwt;
    private final UserRepository users;
    public JwtAuthFilter(JwtService jwt, UserRepository users) { this.jwt=jwt; this.users=users; }

    @Override protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String path=req.getServletPath();
        if (req.getMethod().equals("OPTIONS") || path.equals("/api/login") || path.equals("/error")) { chain.doFilter(req,res); return; }
        String header=req.getHeader("Authorization");
        if (header==null || !header.startsWith("Bearer ") || header.substring(7).isBlank()) {
            deny(res); return;
        }
        try {
            Claims claims=jwt.verify(header.substring(7));
            Long id=Long.valueOf(claims.getSubject());
            AppUser user=users.findById(id).orElseThrow();
            if (!user.getRole().name().equals(claims.get("role",String.class))) { deny(res); return; }
            var auth=new UsernamePasswordAuthenticationToken(user,null,
                    List.of(new SimpleGrantedAuthority("ROLE_"+user.getRole().name())));
            SecurityContextHolder.getContext().setAuthentication(auth);
        } catch (Exception ex) { SecurityContextHolder.clearContext(); deny(res); return; }
        chain.doFilter(req,res);
    }
    private void deny(HttpServletResponse res) throws IOException {
        if (!res.isCommitted()) {
            res.setStatus(401);
            res.setContentType(MediaType.APPLICATION_JSON_VALUE);
            res.getWriter().write("{\"error\":\"invalid or missing token\"}");
        }
    }
}
