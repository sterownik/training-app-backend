package com.example.training.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StravaLapDto {

    private Long id;
    private String name;

    @JsonProperty("elapsed_time")
    private Integer elapsedTime;

    @JsonProperty("moving_time")
    private Integer movingTime;

    private Double distance;

    @JsonProperty("average_speed")
    private Double averageSpeed;

    @JsonProperty("max_speed")
    private Double maxSpeed;

    @JsonProperty("average_heartrate")
    private Double averageHeartrate;

    @JsonProperty("max_heartrate")
    private Double maxHeartrate;

    @JsonProperty("average_watts")
    private Integer averageWatts;

    @JsonProperty("average_cadence")
    private Integer averageCadence;

    // Gettery i Settery
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(Integer elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public Integer getMovingTime() {
        return movingTime;
    }

    public void setMovingTime(Integer movingTime) {
        this.movingTime = movingTime;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public Double getAverageSpeed() {
        return averageSpeed;
    }

    public void setAverageSpeed(Double averageSpeed) {
        this.averageSpeed = averageSpeed;
    }

    public Double getMaxSpeed() {
        return maxSpeed;
    }

    public void setMaxSpeed(Double maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    public Double getAverageHeartrate() {
        return averageHeartrate;
    }

    public void setAverageHeartrate(Double averageHeartrate) {
        this.averageHeartrate = averageHeartrate;
    }

    public Double getMaxHeartrate() {
        return maxHeartrate;
    }

    public void setMaxHeartrate(Double maxHeartrate) {
        this.maxHeartrate = maxHeartrate;
    }

    public Integer getAverageWatts() {
        return averageWatts;
    }

    public void setAverageWatts(Integer averageWatts) {
        this.averageWatts = averageWatts;
    }

    public Integer getAverageCadence() {
        return averageCadence;
    }

    public void setAverageCadence(Integer averageCadence) {
        this.averageCadence = averageCadence;
    }
}
