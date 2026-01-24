package com.example.training.controller;

import com.example.training.model.StravaTokenResponse;
import com.example.training.model.User;
import com.example.training.repository.UserRepository;
import com.example.training.services.StravaAuthService;
import com.example.training.services.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.Instant;

@RestController
@RequestMapping("/auth")
public class StravaAuthController {

    private final UserRepository userRepository;
    private final StravaAuthService stravaAuthService;
    private final JwtService jwtService;

    public StravaAuthController(
            UserRepository userRepository,
            StravaAuthService stravaAuthService,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.stravaAuthService = stravaAuthService;
        this.jwtService = jwtService;
    }

    @GetMapping("/strava")
    public void redirectToStrava(HttpServletResponse response) throws IOException {

        String url =
                "https://www.strava.com/oauth/authorize" +
                        "?client_id=191684" +
                        "&response_type=code" +
                        "&redirect_uri=https://training-app-backend-production-4d0d.up.railway.app/auth/strava/callback" +
                        "&scope=activity:read_all";

        response.sendRedirect(url);
    }

    @GetMapping("/strava/callback")
    public void callback(
            @RequestParam String code,
            HttpServletResponse response
    ) throws IOException {

        StravaTokenResponse token =
                stravaAuthService.exchangeCodeForToken(code);

        User user = userRepository
                .findByStravaAthleteId(token.getAthlete().getId())
                .orElseGet(User::new);

        user.setStravaAthleteId(token.getAthlete().getId());
        user.setFirstname(token.getAthlete().getFirstname());
        user.setLastname(token.getAthlete().getLastname());
        user.setStravaAccessToken(token.getAccess_token());
        user.setStravaRefreshToken(token.getRefresh_token());
        user.setStravaTokenExpiresAt(
                Instant.ofEpochSecond(token.getExpires_at())
        );

        userRepository.save(user);

        String jwt = jwtService.generateToken(user);

        response.sendRedirect(
                "http://localhost:4200/login-success?token=" + jwt +"client-id=" + token.getAthlete().getId()
        );
    }


}
