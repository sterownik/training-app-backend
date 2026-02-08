package com.example.training.security;

import com.example.training.repository.UserRepository;
import com.example.training.services.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    JwtService jwtService;

    @Autowired
    UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException {
        String path = request.getRequestURI();

        // 🔥 IGNORUJ FRONTEND + AUTH
        if (
                path.equals("/") ||
                        path.equals("/index.html") ||
                        path.startsWith("/assets") ||
                        path.startsWith("/media") ||
                        path.startsWith("/auth") ||
                        !path.startsWith("/api/user") ||
                        !path.startsWith("/api/training")
        ) {
            chain.doFilter(request, response);
            return;
        }


        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            Long userId = jwtService.getUserId(token);

            userRepository.findById(userId).ifPresent(user -> {
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                user, null, List.of()
                        );
                SecurityContextHolder.getContext().setAuthentication(auth);
            });
        }

        chain.doFilter(request, response);
    }
}

