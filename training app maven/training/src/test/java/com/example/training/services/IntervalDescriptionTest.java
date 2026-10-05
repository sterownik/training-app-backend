package com.example.training.services;

import com.example.training.model.StravaLapDto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class IntervalDescriptionTest {

    private static StravaLapDto lap(int seconds, Integer watts, Double heartRate) {
        return lap(seconds, watts, heartRate, null);
    }

    private static StravaLapDto lap(int seconds, Integer watts, Double heartRate, Double meters) {
        StravaLapDto lap = new StravaLapDto();
        lap.setDistance(meters);
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
    void mergesConsecutiveLapsAboveThreshold() {
        StravaLapDto[] laps = {
                lap(300, 240, 160.0),
                lap(300, 260, 170.0),   // razem: 10 min, 250 W, 165 bpm
                lap(240, 120, 130.0),
                lap(120, 300, null),
                lap(60, 330, 175.0),    // razem: 3 min, 310 W, tetno tylko z okrazenia z czujnikiem
        };

        assertEquals("10 min interwał 250 W, tętno 165 bpm; 4 min przerwy; 3 min interwał 310 W, tętno 175 bpm",
                StravaActivityService.describeIntervals(laps));
    }

    @Test
    void handlesMissingHeartRate() {
        StravaLapDto[] laps = {lap(270, 300, null), lap(60, 100, null), lap(45, 320, null)};

        assertEquals("4 min 30 s interwał 300 W; 1 min przerwy; 45 s interwał 320 W",
                StravaActivityService.describeIntervals(laps));
    }

    @Test
    void addsDistanceBeforeFirstAndAfterLastInterval() {
        StravaLapDto[] laps = {
                lap(1200, 150, 120.0, 8000.0),
                lap(900, 160, 125.0, 4340.0),   // przed: 12,3 km
                lap(600, 250, 165.0, 6000.0),
                lap(300, 120, 130.0, 2000.0),   // przerwa - nie liczy sie do "po"
                lap(600, 255, 168.0, 6100.0),
                lap(900, 130, 120.0, 8050.0),   // po: 8,1 km
        };

        assertEquals("12,3 km przed interwałami; 10 min interwał 250 W, tętno 165 bpm; 5 min przerwy; "
                        + "10 min interwał 255 W, tętno 168 bpm; 8,1 km po interwałach",
                StravaActivityService.describeIntervals(laps));
    }
}
