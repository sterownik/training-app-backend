package com.example.training.model;

public class ActivityRun extends  ActivityToPromptDetailsDto {
    private Number distance_m;
    private String avg_pace_min_per_km;
    private Number total_elev_gain_m;

    public Number getDistance_m() {
        return distance_m;
    }

    public void setDistance_m(Number distance_m) {
        this.distance_m = distance_m;
    }

    public Number getTotal_elev_gain_m() {
        return total_elev_gain_m;
    }

    public void setTotal_elev_gain_m(Number total_elev_gain_m) {
        this.total_elev_gain_m = total_elev_gain_m;
    }

    public String getAvg_pace_min_per_km() {
        return avg_pace_min_per_km;
    }

    public void setAvg_pace_min_per_km(String avg_pace_min_per_km) {
        this.avg_pace_min_per_km = avg_pace_min_per_km;
    }
}
