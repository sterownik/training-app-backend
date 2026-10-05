package com.example.training.services;

import com.example.training.model.StravaLapDto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class IntervalDescriptionTest {

    private static StravaLapDto lap(int seconds, Integer watts, Double heartRate) {
        StravaLapDto lap = new StravaLapDto();
        lap.setMovingTime(seconds);
        lap.setElapsedTime(seconds);
        lap.setAverageWatts(watts);
        lap.setAverageHeartrate(heartRate);
        return lap;
    }

    @Test
    void describesIntervalsAndRestsBetweenThem() {
        StravaLapDto[] laps = {
                lap(900, 150, 120.0),   // rozgrzewka - pomijana
                lap(600, 240, 165.4),
                lap(180, 120, 130.0),
                lap(120, 100, 125.0),   // przerwa = 180 + 120 s
                lap(601, 245, 170.0),
                lap(600, 130, 120.0),   // schlodzenie - pomijane
        };

        assertEquals(
                "10 min interwał 240 W, tętno 165 bpm; 5 min przerwy; 10 min interwał 245 W, tętno 170 bpm",
                StravaActivityService.describeIntervals(laps));
    }

    @Test
    void returnsNullWhenNoLapAboveThreshold() {
        StravaLapDto[] laps = {lap(1800, 200, 140.0), lap(1800, 220, 150.0), lap(600, null, null)};

        assertNull(StravaActivityService.describeIntervals(laps));
    }

    @Test
    void handlesMissingHeartRateAndBackToBackIntervals() {
        StravaLapDto[] laps = {lap(270, 300, null), lap(45, 320, null)};

        assertEquals("4 min 30 s interwał 300 W; 45 s interwał 320 W",
                StravaActivityService.describeIntervals(laps));
    }
}
