package com.example.training.model;

public class ActivityBike extends ActivityToPromptDetailsDto {
    private Number distance_m;
          private Number avg_watts;
          private Number normalized_power;
          private Number avg_speed_km_h;
          private Number max_speed_km_h;
          private Number total_elevation_gain_m;

    public Number getDistance_m() {
        return distance_m;
    }

    public void setDistance_m(Number distance_m) {
        this.distance_m = distance_m;
    }

    public Number getAvg_watts() {
        return avg_watts;
    }

    public void setAvg_watts(Number avg_watts) {
        this.avg_watts = avg_watts;
    }

    public Number getAvg_speed_km_h() {
        return avg_speed_km_h;
    }

    public void setAvg_speed_km_h(Number avg_speed) {
        this.avg_speed_km_h = avg_speed;
    }

    public Number getMax_speed_km_h() {
        return max_speed_km_h;
    }

    public void setMax_speed_km_h(Number max_speed) {
        this.max_speed_km_h = max_speed;
    }

    public Number getTotal_elevation_gain_m() {
        return total_elevation_gain_m;
    }

    public void setTotal_elevation_gain_m(Number total_elevation_gain_m) {
        this.total_elevation_gain_m = total_elevation_gain_m;
    }

    public Number getNormalized_power() {
        return normalized_power;
    }

    public void setNormalized_power(Number normalized_power) {
        this.normalized_power = normalized_power;
    }
}
