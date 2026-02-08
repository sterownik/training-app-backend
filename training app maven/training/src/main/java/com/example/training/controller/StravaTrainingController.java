package com.example.training.controller;

import com.example.training.model.*;
import com.example.training.repository.ActivityRepository;
import com.example.training.repository.UserRepository;
import com.example.training.services.OpenAiService;
import com.example.training.services.StravaActivityService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/training")
public class StravaTrainingController {
    private final UserRepository userRepository;
    private final StravaActivityService stravaActivityService;
    private final OpenAiService openAiService;
    private final ActivityRepository activityRepository;

    public StravaTrainingController(UserRepository userRepository, StravaActivityService stravaActivityService, OpenAiService openAiService, ActivityRepository activityRepository) {
        this.userRepository = userRepository;
        this.stravaActivityService = stravaActivityService;
        this.openAiService = openAiService;
        this.activityRepository = activityRepository;
    }


    @GetMapping("/activities/last-year")
    public String getActivitiesLastYear(Authentication auth) {

        User user = (User) auth.getPrincipal();

        stravaActivityService
                .getActivitiesLastYear(user.getStravaAccessToken(), user);
        return "OK";
    }

    @GetMapping("/activities")
    public Page<ActivityDto> callback(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy
    ) throws IOException {
        User user = (User) auth.getPrincipal();
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
        return activityRepository.findByUserIdOrderByStartDateLocalDesc(user.getId(), pageable)
                .map(ActivityDto::from);
    }

    @PostMapping("/send-to-chat")
    public String sendToChat(@RequestBody Prompt prompt, Authentication auth) throws IOException {
//        return prompt.getPrompt();
        User user = (User) auth.getPrincipal();
        ReadyToSendAi activitiesToPromptDto = stravaActivityService.prepareData(user, prompt.getFilterActivityType());
        String systemPrompt =prompt.getPrompt();

        try {
            return openAiService.analyzeTraining(activitiesToPromptDto, systemPrompt);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/update-activity")
        public void updateActivity(@RequestBody UpdateActivity updateActivity, Authentication auth) {
            User user = (User) auth.getPrincipal();
        Activity a = activityRepository.findByIdAndUserId(updateActivity.getId(), user.getId());

        if(updateActivity.getDescription() != null) {
            a.setDescription(updateActivity.getDescription());
        }
        if(updateActivity.getNpPower() != null) {
            a.setNormalizedPower(updateActivity.getNpPower());
        }
        activityRepository.save(a);
    }
}
