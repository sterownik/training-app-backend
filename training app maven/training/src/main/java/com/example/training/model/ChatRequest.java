package com.example.training.model;

import java.util.UUID;

public record ChatRequest(
        UUID idChat ,
        String message
) {
}
