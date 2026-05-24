package com.example.training.controller;

import com.example.training.model.*;
import com.example.training.repository.ActivityRepository;
import com.example.training.repository.ChatMessageRepository;
import com.example.training.repository.UserRepository;
import com.example.training.services.ChatService;
import com.example.training.services.ExportService;
import com.example.training.services.OpenAiService;
import com.example.training.services.StravaActivityService;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.stream;

@RestController
@RequestMapping("/api/training")
public class StravaTrainingController {
    private final UserRepository userRepository;
    private final StravaActivityService stravaActivityService;
    private final OpenAiService openAiService;
    private final ExportService exportService;
    private final ActivityRepository activityRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatService chatService;

    public StravaTrainingController(UserRepository userRepository, StravaActivityService stravaActivityService, OpenAiService openAiService, ExportService exportService, ActivityRepository activityRepository, ChatMessageRepository chatMessageRepository, ChatService chatService) {
        this.userRepository = userRepository;
        this.stravaActivityService = stravaActivityService;
        this.openAiService = openAiService;
        this.exportService = exportService;
        this.activityRepository = activityRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.chatService = chatService;
    }


    @GetMapping("/activities/last-year")
    public Map<String, String>  getActivitiesLastYear(Authentication auth) throws InterruptedException {

        User user = (User) auth.getPrincipal();

        stravaActivityService
                .getActivitiesLastYear(user.getStravaAccessToken(), user);

        stravaActivityService
                .updateLaps(user.getStravaAccessToken(), user);
        Map<String, String> response = new HashMap<>();
        response.put("message", "ok");
        return response;
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

    @GetMapping("/activities-min")
    public Stream<ActivityMinDto> activitiesMin(
            Authentication auth) {
        User user = (User) auth.getPrincipal();
        return activityRepository.findFirst10ByUserIdOrderByStartDateLocalDesc(user.getId()).stream().map(ActivityMinDto::from);
    }

    @PostMapping("/send-to-chat")
    public ChatResponse sendToChat(@RequestBody ChatRequest prompt, Authentication auth) throws Exception {
//        return prompt.getPrompt();
        User user = (User) auth.getPrincipal();
        return chatService.chat(prompt, user);
    }

    @PostMapping("/update-activity")
        public void updateActivity(@RequestBody UpdateActivity updateActivity, Authentication auth) {
            User user = (User) auth.getPrincipal();
        Activity a = activityRepository.findByIdAndUserId(updateActivity.getId(), user.getId());

        if(updateActivity.getDescriptionTyped() != null) {
            a.setDescriptionTyped(updateActivity.getDescriptionTyped());
        }
        if(updateActivity.getNpPower() != null) {
            a.setNormalizedPower(updateActivity.getNpPower());
        }
        activityRepository.save(a);
    }

    @GetMapping("/activities/excel")
    public ResponseEntity<byte[]> downloadExcel(Authentication auth) throws IOException {
        User user = (User) auth.getPrincipal();
        HSSFWorkbook workbook = exportService.export(activityRepository.findAllByUserOrderByStartDateLocalDesc(user)); // Twoja metoda

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        byte[] bytes = out.toByteArray();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(
                ContentDisposition.builder("attachment")
                        .filename("aktywności.csv")
                        .build()
        );

        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }

    @GetMapping("/last-chat")
    public List<ChatMessage> lastChat(
            Authentication auth) {
        User user = (User) auth.getPrincipal();
        return chatMessageRepository.findLatestChatMessages(user.getId());
    }
}
