package com.example.training.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "activities", uniqueConstraints = @UniqueConstraint(columnNames = "stravaActivityId"))
@Getter
@Setter
public class Activity
 {

     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     private Long id;

     @Column(nullable = false)
     private Long stravaActivityId;

     @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "user_id")
     private User user;

     @Column(length = 2000)
     private String type;


     @Lob
     @Column(length = 5000, columnDefinition = "TEXT")
     private String laps;

     @Column(length = 2000)
     private String description;

     @Column(length = 2000)
     private Double distance;

     @Column(length = 2000,nullable = true)
     private Double normalizedPower;

     @Column(length = 2000)
     private Double averageHeartRate;

     @Column(length = 2000)
     private Double maxSpeed;

     @Column(length = 2000)
     private Double maxHeartRate;

     @Column(length = 2000)
     private String startCity;

     @Column(length = 2000)
     private String descriptionTyped;

     @Column(length = 2000)
     private String photoUrl;

     @Column(length = 2000)
     private Double averageWatts;

     @Column(length = 2000)
     private Double moving_time;

     @Column(length = 2000)
     private Double weightedAverageWatts;

     @Column(length = 2000)
     private Long elapsedTime;

     @Column(length = 2000)
     private Double calories;

     @Column(length = 2000)
     private Double averageSpeed;

     @Column(length = 2000)
     private Double totalElevationGain;

     @Column(length = 2000)
     private OffsetDateTime startDateLocal;

     public String getType() {
         return type;
     }

     public void setType(String type) {
         this.type = type;
     }

     public Double getDistance() {
         return distance;
     }

     public void setDistance(Double distance) {
         this.distance = distance;
     }

     public Double getAverageHeartRate() {
         return averageHeartRate;
     }

     public void setAverageHeartRate(Double averageHeartRate) {
         this.averageHeartRate = averageHeartRate;
     }

     public Double getMaxHeartRate() {
         return maxHeartRate;
     }

     public void setMaxHeartRate(Double maxHeartRate) {
         this.maxHeartRate = maxHeartRate;
     }

     public String getStartCity() {
         return startCity;
     }

     public void setStartCity(String startCity) {
         this.startCity = startCity;
     }

     public Double getAverageWatts() {
         return averageWatts;
     }

     public void setAverageWatts(Double averageWatts) {
         this.averageWatts = averageWatts;
     }

     public Double getWeightedAverageWatts() {
         return weightedAverageWatts;
     }

     public void setWeightedAverageWatts(Double weightedAverageWatts) {
         this.weightedAverageWatts = weightedAverageWatts;
     }

     public Long getElapsedTime() {
         return elapsedTime;
     }

     public void setElapsedTime(Long elapsedTime) {
         this.elapsedTime = elapsedTime;
     }

     public Double getCalories() {
         return calories;
     }

     public void setCalories(Double calories) {
         this.calories = calories;
     }

     public Double getAverageSpeed() {
         return averageSpeed;
     }

     public void setAverageSpeed(Double averageSpeed) {
         this.averageSpeed = averageSpeed;
     }

     public Double getTotalElevationGain() {
         return totalElevationGain;
     }

     public void setTotalElevationGain(Double totalElevationGain) {
         this.totalElevationGain = totalElevationGain;
     }

     public Double getMaxSpeed() {
         return maxSpeed;
     }

     public void setMaxSpeed(Double maxSpeed) {
         this.maxSpeed = maxSpeed;
     }

     public Long getStravaActivityId() {
         return stravaActivityId;
     }

     public void setStravaActivityId(Long stravaActivityId) {
         this.stravaActivityId = stravaActivityId;
     }

     public User getUser() {
         return user;
     }

     public void setUser(User user) {
         this.user = user;
     }

     public OffsetDateTime  getStartDateLocal() {
         return startDateLocal;
     }

     public void setStartDateLocal(OffsetDateTime  startDateLocal) {
         this.startDateLocal = startDateLocal;
     }

     public Long getId() {
         return id;
     }

     public void setId(Long id) {
         this.id = id;
     }

     public Double getMoving_time() {
         return moving_time;
     }

     public void setMoving_time(Double moving_time) {
         this.moving_time = moving_time;
     }

     public Double getNormalizedPower() {
         return normalizedPower;
     }

     public void setNormalizedPower(Double normalizedPower) {
         this.normalizedPower = normalizedPower;
     }

     public String getDescription() {
         return description;
     }

     public void setDescription(String description) {
         this.description = description;
     }

     public String getDescriptionTyped() {
         return descriptionTyped;
     }

     public void setDescriptionTyped(String descriptionTyped) {
         this.descriptionTyped = descriptionTyped;
     }

     public String getPhotoUrl() {
         return photoUrl;
     }

     public void setPhotoUrl(String photoUrl) {
         this.photoUrl = photoUrl;
     }

     public String getLaps() {
         return laps;
     }

     public void setLaps(String laps) {
         this.laps = laps;
     }
 }
