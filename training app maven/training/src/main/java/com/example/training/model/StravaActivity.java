package com.example.training.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StravaActivity {
    private String type;
    private Double distance; // w metrach
    private Double average_heartrate;
    private Double max_heartrate;
    @JsonProperty("start_city")
    private String start_city;
    private String description;
    private Double average_watts;
    private Double weighted_average_watts; // normalizowane waty
    private Long elapsed_time; // sekundy
    @JsonProperty("calories")
    private Double calories;
    private Double average_speed;
    private Double moving_time;
    private Double total_elevation_gain;
    private Double max_speed;
    private Long id;
    @JsonProperty("start_date_local")
    private OffsetDateTime startDateLocal;

    private Photos photos;

    public Double getAverage_watts() {
        return average_watts;
    }

    public void setAverage_watts(Double average_watts) {
        this.average_watts = average_watts;
    }

    public Double getWeighted_average_watts() {
        return weighted_average_watts;
    }

    public void setWeighted_average_watts(Double weighted_average_watts) {
        this.weighted_average_watts = weighted_average_watts;
    }

    public Long getElapsed_time() {
        return elapsed_time;
    }

    public void setElapsed_time(Long elapsed_time) {
        this.elapsed_time = elapsed_time;
    }

    public Double getCalories() {
        return calories;
    }

    public void setCalories(Double calories) {
        this.calories = calories;
    }

    public Double getAverage_speed() {
        return average_speed;
    }

    public void setAverage_speed(Double average_speed) {
        this.average_speed = average_speed;
    }

    public Double getTotal_elevation_gain() {
        return total_elevation_gain;
    }

    public void setTotal_elevation_gain(Double total_elevation_gain) {
        this.total_elevation_gain = total_elevation_gain;
    }

    public void setMax_heartrate(Double max_heartrate) {
        this.max_heartrate = max_heartrate;
    }

    public void setAverage_heartrate(Double average_heartrate) {
        this.average_heartrate = average_heartrate;
    }

    public void setStart_city(String start_city) {
        this.start_city = start_city;
    }

    public Double getMax_speed() {
        return max_speed;
    }

    public void setMax_speed(Double max_speed) {
        this.max_speed = max_speed;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public OffsetDateTime getStartDateLocal() {
        return startDateLocal;
    }

    public void setStartDateLocal(OffsetDateTime startDateLocal) {
        this.startDateLocal = startDateLocal;
    }

    public Double getMoving_time() {
        return moving_time;
    }

    public void setMoving_time(Double moving_time) {
        this.moving_time = moving_time;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Photos {
        private PhotoSummary primary;

        public PhotoSummary getPrimary() { return primary; }
        public void setPrimary(PhotoSummary primary) { this.primary = primary; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PhotoSummary {
        private Map<String, String> urls; // JSON z mapą rozdzielczości, np. "600":"https://..."
        public Map<String, String> getUrls() { return urls; }
        public void setUrls(Map<String, String> urls) { this.urls = urls; }
    }

    // gettery / settery
    public String getType() { return type; }
    public Double getDistance() { return distance; }
    public Double getAverage_heartrate() { return average_heartrate; }
    public Double getMax_heartrate() { return max_heartrate; }
    public String getStart_city() { return start_city; }
    public Photos getPhotos() { return photos; }
}

