package com.example.training.services;

import com.example.training.model.ReadyToSendAi;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@Service
public class OpenAiService {

    private static final String URL = "https://api.openai.com/v1/chat/completions";

    @Value("${openai.api.key}")
    private String apiKey;

    private final ObjectMapper mapper = new ObjectMapper();

    public String analyzeTraining(ReadyToSendAi data, String SYSTEM_PROMPT) throws Exception {

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(110, TimeUnit.SECONDS)
                .readTimeout(310, TimeUnit.SECONDS)
                .writeTimeout(310, TimeUnit.SECONDS)
                .build();

        String bodyJson = mapper.writeValueAsString(
                Map.of(
                        "model", "gpt-4.1",
                        "messages", List.of(
                                Map.of("role", "system", "content", SYSTEM_PROMPT),
                                Map.of("role", "user", "content", mapper.writeValueAsString(data))
                        )
                )
        );

        Request request = new Request.Builder()
                .url(URL)
                .post(RequestBody.create(bodyJson, MediaType.parse("application/json")))
                .addHeader("Authorization", "Bearer " + apiKey)
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }
}
