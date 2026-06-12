package com.example.training.controller;

import com.example.training.model.User;
import com.example.training.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public Optional<User> meEnpoint(
            Authentication auth,
            HttpServletResponse response
    ) throws IOException {
        User user = (User) auth.getPrincipal();
        Optional<User> findedUser = userRepository
                .findById(user.getId());
        return findedUser;
    }

    @GetMapping("/athlete-info")
    public Optional<String> athleteInfo(
            Authentication auth
    ) throws IOException {
        User user = (User) auth.getPrincipal();
        Optional<String> info = userRepository.findById(user.getId())
                .map(User::getAthleteInfo);
        return info;
    }


    @PostMapping("/athlete-info")
    public void saveAthleteInfo(
            Authentication auth,
            @RequestBody String athleteInfo
    ) {
        User user = (User) auth.getPrincipal();

        User dbUser = userRepository.findById(user.getId())
                .orElseThrow();

        dbUser.setAthleteInfo(athleteInfo);
        userRepository.save(dbUser);
    }

}
