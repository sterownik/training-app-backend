package com.example.training.model;

import com.example.training.model.Activity;

import java.time.OffsetDateTime;

public record ActivityDto(
        Long id,
        String type,
        Double distance,
        Double averageSpeed,
        OffsetDateTime startDateLocal
) {
    public static ActivityDto from(Activity a) {
        return new ActivityDto(
                a.getId(),
                a.getType(),
                a.getDistance(),
                a.getAverageSpeed(),
                a.getStartDateLocal()
        );
    }
}
