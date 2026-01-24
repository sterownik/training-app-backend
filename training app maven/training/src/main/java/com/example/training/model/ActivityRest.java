package com.example.training.model;

public class ActivityRest extends ActivityToPromptDetailsDto {
    private Number distance_m;
    private String type_activity;

    public Number getDistance_m() {
        return distance_m;
    }

    public void setDistance_m(Number distance_m) {
        this.distance_m = distance_m;
    }

    public String getType_activity() {
        return type_activity;
    }

    public void setType_activity(String type_activity) {
        this.type_activity = type_activity;
    }
}
