package com.example.training.model;

import java.util.ArrayList;

public class ActivityToPromptDetailsDto {
    private Number elapsed_time_in_sec;
    private Number avg_heart_rate_bpm;
    private Number max_heart_rate_bpm;
    private String date;
    private Number strava_activity_id;
    private String description;


    public Number getAvg_heart_rate_bpm() {
        return avg_heart_rate_bpm;
    }

    public void setAvg_heart_rate_bpm(Number avg_heart_rate_bpm) {
        this.avg_heart_rate_bpm = avg_heart_rate_bpm;
    }

    public Number getMax_heart_rate_bpm() {
        return max_heart_rate_bpm;
    }

    public void setMax_heart_rate_bpm(Number max_heart_rate_bpm) {
        this.max_heart_rate_bpm = max_heart_rate_bpm;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Number getStrava_activity_id() {
        return strava_activity_id;
    }

    public void setStrava_activity_id(Number strava_activity_id) {
        this.strava_activity_id = strava_activity_id;
    }

    public Number getElapsed_time_in_sec() {
        return elapsed_time_in_sec;
    }

    public void setElapsed_time_in_sec(Number elapsed_time_in_sec) {
        this.elapsed_time_in_sec = elapsed_time_in_sec;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
