package com.example.training.model;

import com.example.training.model.Activity;

import java.time.OffsetDateTime;

public record ActivityDto(
        Long id,
        String type,
        String distance,
        Double averageSpeed,
        OffsetDateTime startDateLocal,
        Double averageWatts,
        String description,
        String photoUrl,
        String moving_time,
        Double averageHeartRate,
        String pace
) {
    public static ActivityDto from(Activity a) {
        String pace;
        if(a.getType().contains("Ride")) {
            pace = calculateAverageSpeed(a.getDistance(), a.getMoving_time()) + " km/h";
        } else {
            pace = paceFromDistanceAndMovingTime(a.getDistance(), a.getMoving_time());
        }
        String movingTime;
        int hours = (int) (a.getMoving_time() / 3600);
        int minutes = (int) (a.getMoving_time() / 60);
        int seconds = (int) Math.round(a.getMoving_time() % 60);
        if(hours > 0) {
            movingTime = hours + " h " + (int)((a.getMoving_time() - (hours * 3600))/60) + " m";
        }
        else {
            movingTime = minutes + " m " + seconds + " s";
        }

        if (seconds == 60) {
            minutes++;
            seconds = 0;
        }
        return new ActivityDto(
                a.getId(),
                a.getType(),
                String.format("%.2f", a.getDistance() / 1000)+" km",
                a.getAverageSpeed(),
                a.getStartDateLocal(),
                a.getAverageWatts(),
                a.getDescriptionTyped(),
                a.getPhotoUrl(),
                movingTime,
                a.getAverageHeartRate(),
                pace
        );
    }

    public static String paceFromDistanceAndMovingTime(
            Double distanceMeters,
            Double movingTimeSeconds
    ) {
        if (distanceMeters == null || distanceMeters <= 0
                || movingTimeSeconds == null || movingTimeSeconds <= 0) {
            return null;
        }

        double paceSecondsPerKm =
                movingTimeSeconds / (distanceMeters / 1000.0);

        int minutes = (int) (paceSecondsPerKm / 60);
        int seconds = (int) Math.round(paceSecondsPerKm % 60);

        if (seconds == 60) {
            minutes++;
            seconds = 0;
        }

        return String.format("%d:%02d min/km", minutes, seconds);
    }

    public static double calculateAverageSpeed(double distanceMeters, double movingTimeSeconds) {
        if (movingTimeSeconds == 0) {
            return 0;
        }
        double distanceKm = distanceMeters / 1000.0;
        double timeHours = movingTimeSeconds / 3600.0;
        double speed = distanceKm / timeHours;

        return Math.round(speed * 100.0) / 100.0;
    }
}
