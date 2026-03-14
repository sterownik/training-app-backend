package com.example.training.model;

import java.time.OffsetDateTime;

public record ActivityMinDto(
        Long id,
        String type,
        OffsetDateTime startDateLocal,
        String description
) {
    public static ActivityMinDto from(Activity a) {
        return new ActivityMinDto(a.getId(), a.getType(), a.getStartDateLocal(), a.getDescription());
    }
}
