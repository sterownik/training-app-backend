package com.example.training.services;

import com.example.training.model.*;
import com.example.training.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository repository;
    private final StravaActivityService stravaActivityService;
    private final OpenAiService openAiService;

    public ChatResponse chat(ChatRequest request, User user) throws Exception {
        UUID idChat = request.idChat();
        if (idChat == null) {
            idChat = UUID.randomUUID();
        }

        // zapis user message
        repository.save(
                ChatMessage.builder()
                        .idChat(idChat)
                        .userId(user.getId())
                        .message(request.message())
                        .messageType(MessageType.USER)
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        // pobranie kontekstu
        List<ChatMessage> context =
                repository.findTop10ByIdChatOrderByCreatedAtDesc(
                        idChat
                );

        // ważne -> odwrócenie kolejności
        context = context.stream()
                .sorted(Comparator.comparing(ChatMessage::getCreatedAt))
                .toList();

        // tutaj budujesz prompt
        StringBuilder prompt = new StringBuilder();
        ReadyToSendAi activitiesToPromptDto = stravaActivityService.prepareData(user
//                prompt.getFilterActivityType()
        );

        for (ChatMessage msg : context) {

            prompt.append(msg.getMessageType())
                    .append(": ")
                    .append(msg.getMessage())
                    .append("\n");
        }

        String aiResponse;
        ObjectMapper mapper = new ObjectMapper();


        try {
            aiResponse = openAiService.analyzeTraining(activitiesToPromptDto, String.valueOf(prompt));
            JsonNode root = mapper.readTree(aiResponse);
            aiResponse = root
                    .get("choices")
                    .get(0)
                    .get("message")
                    .get("content")
                    .asText();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // tutaj później OpenAI / Spring AI
//        String aiResponse = "Tutaj odpowiedź AI";

        // zapis odpowiedzi
        repository.save(
                ChatMessage.builder()
                        .idChat(idChat)
                        .userId(user.getId())
                        .message(aiResponse)
                        .messageType(MessageType.ASSISTANT)
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        return new ChatResponse(aiResponse);
    }
}
