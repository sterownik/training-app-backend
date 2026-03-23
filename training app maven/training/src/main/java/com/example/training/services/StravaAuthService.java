package com.example.training.services;

import com.example.training.model.StravaTokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class StravaAuthService {

    private static final String TOKEN_URL = "https://www.strava.com/oauth/token";

//    @Value("${strava.client-id}")
    private final String clientId = "191684";

//    @Value("${strava.client-secret}")
    private final String clientSecret = "0fdf942eed9b82efe708a31fe89c2e0bb96573f2";

    private final RestClient restClient = RestClient.create();

    public StravaTokenResponse exchangeCodeForToken(String code) {

        return restClient
                .post()
                .uri(TOKEN_URL)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(
                        new LinkedMultiValueMap<>() {{
                            add("client_id", clientId);
                            add("client_secret", clientSecret);
                            add("code", code);
                            add("grant_type", "authorization_code");
                        }}
                )
                .retrieve()
                .body(StravaTokenResponse.class);
    }

    public StravaTokenResponse refreshAccessToken(String refreshToken) {
        Map<String, String> body = new HashMap<>();
        body.put("client_id", clientId);
        body.put("client_secret", clientSecret);
        body.put("grant_type", "refresh_token");
        body.put("refresh_token", refreshToken);

        RestTemplate restTemplate = new RestTemplate();

        return restTemplate.postForObject(
                TOKEN_URL,
                body,
                StravaTokenResponse.class
        );
    }
}
