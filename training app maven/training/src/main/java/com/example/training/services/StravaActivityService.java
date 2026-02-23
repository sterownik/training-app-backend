package com.example.training.services;

import com.example.training.model.*;
import com.example.training.repository.ActivityRepository;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class StravaActivityService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ActivityRepository activityRepository;
    private final OpenAiService openAiService;

    public StravaActivityService(ActivityRepository activityRepository, OpenAiService openAiService) {
        this.activityRepository = activityRepository;
        this.openAiService = openAiService;
    }

    public void updateLaps(String accessToken, User user) throws InterruptedException {
        List<Activity> activities = activityRepository
                .findFirst60ByUserIdAndLapsIsNullOrderByStartDateLocalDesc(user.getId());

        for (Activity activity : activities) {


            if(activity.getLaps() == null && (activity.getType().contains("Ride") || activity.getType().contains("Run"))) {
                try {
                    String url = "https://www.strava.com/api/v3/activities/" + activity.getStravaActivityId() + "/laps";

                    HttpHeaders headers = new HttpHeaders();
                    headers.setBearerAuth(accessToken);

                    HttpEntity<String> entity = new HttpEntity<>(headers);

                    ResponseEntity<StravaLapDto[]> response = restTemplate.exchange(
                            url,
                            HttpMethod.GET,
                            entity,
                            StravaLapDto[].class
                    );

                    StravaLapDto[] lapsStrava = response.getBody();
                    StringBuilder descriptionBuilder = new StringBuilder();

                    if (lapsStrava != null && lapsStrava.length > 0) {
                        switch (activity.getType()) {
                            case "Ride":
                                for (StravaLapDto lapStrava : lapsStrava) {
                                    descriptionBuilder.append("\n")
                                            .append(lapStrava.getName())
                                            .append(" -dystans- ")
                                            .append(lapStrava.getDistance())
                                            .append(" -śr tętno- ")
                                            .append(lapStrava.getAverageHeartrate())
                                            .append(" -tempo- ")
                                            .append(calculateAverageSpeed(lapStrava.getDistance(), lapStrava.getMovingTime()))
                                            .append(" -śr waty- ")
                                            .append(lapStrava.getAverageWatts() != null ? lapStrava.getAverageWatts() : "-")
                                            .append(" -śr kadencja- ")
                                            .append(lapStrava.getAverageCadence() != null ? lapStrava.getAverageCadence() : "-")
                                            .append(" -czas- ")
                                            .append(lapStrava.getMovingTime())
                                            .append(" -max tętno- ")
                                            .append(lapStrava.getMaxHeartrate());
                                }
                                break;
                            case "Run":
                                for (StravaLapDto lapStrava : lapsStrava) {
                                    descriptionBuilder.append("\n")
                                            .append(lapStrava.getName())
                                            .append(" -dystans- ")
                                            .append(lapStrava.getDistance())
                                            .append(" -śr tętno- ")
                                            .append(lapStrava.getAverageHeartrate())
                                            .append(" -tempo- ")
                                            .append(paceFromDistanceAndMovingTime(lapStrava.getDistance(), Double.valueOf(lapStrava.getMovingTime())))
                                            .append(" -maks tętno- ")
                                            .append(lapStrava.getMaxHeartrate());
                                }
                                break;

                            default:
                                break;
                        }

                    }
                    if (!descriptionBuilder.toString().isEmpty()) {
                        activity.setLaps(descriptionBuilder.toString());
                        activityRepository.save(activity);
                        Thread.sleep(100);
                    }
                }catch (HttpClientErrorException.TooManyRequests e) {
                    // Jeśli dostaniesz 429, przerwij pętlę i spróbuj przy następnym uruchomieniu
                    System.err.println("Przekroczono limit Stravy (429). Przerywam sesję.");
                    break;
                } catch (Exception e) {
                    System.err.println("Błąd dla aktywności " + activity.getId() + ": " + e.getMessage());
                }

            }
    }
    }

    public void getActivitiesLastYear(String accessToken, User user) {

        Optional<OffsetDateTime> latest =
                activityRepository
                        .findFirstByUserIdOrderByStartDateLocalDesc(user.getId())
                        .map(Activity::getStartDateLocal);

//
        long before = Instant.now().getEpochSecond();
        long after = latest
                .map(d -> d.toInstant().getEpochSecond())
                .orElseGet(() ->
                        Instant.now()
                                .minus(734, ChronoUnit.DAYS)
                                .getEpochSecond()
                );

        List<Activity> result = new ArrayList<>();

        int page = 1;
        while (true) {

            String url = UriComponentsBuilder
                    .fromUriString("https://www.strava.com/api/v3/athlete/activities")
                    .queryParam("after", after)
                    .queryParam("before", before)
                    .queryParam("page", page)
                    .queryParam("per_page", 200)
                    .toUriString();


            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<StravaActivity[]> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.GET,
                            entity,
                            StravaActivity[].class
                    );

            StravaActivity[] activities = response.getBody();

            if (activities == null || activities.length == 0) {
                break;
            }

            for (StravaActivity a : activities) {
                if(activityRepository.existsByStravaActivityId(a.getId())) {
                    continue;
                }
                Double averageSpeed;
                Double maxSpeed;
                averageSpeed = a.getAverage_speed();
                maxSpeed = a.getMax_speed();
                if(a.getType() == "Ride") {
                    averageSpeed = a.getAverage_speed() != null
                            ? a.getAverage_speed() * 3.6
                            : null;
                    maxSpeed = a.getMax_speed() != null
                            ? a.getMax_speed() * 3.6
                            : null;
                }




                Activity activityDto = new Activity();
                activityDto.setDistance(a.getDistance());
                activityDto.setType(a.getType());
                activityDto.setAverageHeartRate(a.getAverage_heartrate());
                activityDto.setStartCity(a.getStart_city());
                activityDto.setMoving_time(a.getMoving_time());
                activityDto.setUser(user);
                activityDto.setCalories(a.getCalories());
                activityDto.setAverageSpeed(averageSpeed);
                activityDto.setAverageWatts(a.getAverage_watts());
                activityDto.setWeightedAverageWatts(a.getWeighted_average_watts());
                activityDto.setMaxHeartRate(a.getMax_heartrate());
                activityDto.setTotalElevationGain(a.getTotal_elevation_gain());
                activityDto.setElapsedTime(a.getElapsed_time());
                activityDto.setMaxSpeed(maxSpeed);
                activityDto.setStravaActivityId(a.getId());
                activityDto.setStartDateLocal(a.getStartDateLocal());
                System.out.println(a.getPhotos());
                if(a.getPhotos() != null && a.getPhotos().getPrimary().getUrls() != null && a.getPhotos().getPrimary().getUrls().get("600") != null ) {
                    activityDto.setPhotoUrl(a.getPhotos().getPrimary().getUrls().get("600"));
                }

                activityDto.setDescription(a.getName());



                result.add(activityDto);
            }

            page++;
        }
        activityRepository.saveAll(result);
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

        return String.format("%d:%02d /km", minutes, seconds);
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

    public static double convertMsToKmh(double speedMetersPerSecond) {
        double speedKmh = speedMetersPerSecond * 3.6;
        return Math.round(speedKmh * 100.0) / 100.0;
    }

    public ReadyToSendAi prepareData(User user, FilterActivityType filterActivityType) {
        ActivitiesToPromptDto activitiesToPromptDto = new ActivitiesToPromptDto();
        AcitivityBase<ActivityBike> actionsBike = new AcitivityBase<ActivityBike>();
        AcitivityBase<ActivityRun> actionsRun = new AcitivityBase<ActivityRun>();
        AcitivityBase<ActivityWeightTraining> actionsWeightTraining = new AcitivityBase<ActivityWeightTraining>();
        AcitivityBase<ActivityRest> actionsRest = new AcitivityBase<ActivityRest>();
        actionsBike.setActivity(new ArrayList<ActivityBike>());
        actionsRun.setActivity(new ArrayList<ActivityRun>());
        actionsWeightTraining.setActivity(new ArrayList<ActivityWeightTraining>());
        actionsRest.setActivity(new ArrayList<ActivityRest>());
        actionsBike.setType("Ride");
        actionsRun.setType("Run");
        actionsWeightTraining.setType("Weight Training");
        actionsRest.setType("Rest activities");
        actionsBike.setDescription("predkosc w km/h dystans w metrach czas w sekundach przewyzszenie w metrach tetno w bpm");
        actionsRun.setDescription("predkosc w km/h dystans w metrach czas w sekundach przewyzszenie w metrach  tetno w bpm");
        actionsWeightTraining.setDescription("czas w sekundach najczesciej na treningu robie nogi 5 min rowerku goblet squad, step up hipthrust, plank wspecie na palce, wykroki  tetno w bpm");
        actionsRest.setDescription("rozne aktywnosci predkosc w km/h dystans w metrach czas w sekundach przewyzszenie w metrach  tetno w bpm");
        List<Activity> activities;
        System.out.println(filterActivityType.getFilterType());
        if(filterActivityType.getFilterType().contains("date")) {
            activities = activityRepository.findByUserIdAndStartByStartDateLocalAndEnd(user.getId(), filterActivityType.getStartDateLocalStart(), filterActivityType.getStartDateLocalEnd());
        } else {
            activities = activityRepository.findByUserIdOrderByStartDateLocalDesc(user.getId());
        }

        for (Activity a : activities) {
            ActivityBike activityBike = new ActivityBike();
            ActivityRun activityRun = new ActivityRun();
            ActivityWeightTraining activityWeightTraining = new ActivityWeightTraining();
            ActivityRest activityRest = new ActivityRest();

            switch (a.getType()) {
                case "Ride":
                    activityBike.setElapsed_time_in_sec(a.getElapsedTime());
                    activityBike.setDistance_m(a.getDistance());
                    activityBike.setAvg_heart_rate_bpm(a.getAverageHeartRate());
                    activityBike.setMax_heart_rate_bpm(a.getMaxHeartRate());
                    activityBike.setDate(a.getStartDateLocal().toLocalDate() + "");
                    activityBike.setAvg_watts(a.getAverageWatts());
                    activityBike.setAvg_speed_km_h(calculateAverageSpeed(a.getDistance(), a.getMoving_time()));
                    activityBike.setMax_speed_km_h(convertMsToKmh(a.getMaxSpeed()));
                    activityBike.setTotal_elevation_gain_m(a.getTotalElevationGain());
                    activityBike.setStrava_activity_id(a.getStravaActivityId());
                    activityBike.setNormalized_power(a.getNormalizedPower());
                    activityBike.setDescription(a.getDescription() + (a.getDescriptionTyped() == null ? "" : " " + a.getDescriptionTyped()));
                    actionsBike.getActivity().add(activityBike);
                    break;

                case "Run":
                    activityRun.setAvg_pace_min_per_km(paceFromDistanceAndMovingTime(a.getDistance(), a.getMoving_time()));
                    activityRun.setElapsed_time_in_sec(a.getElapsedTime());
                    activityRun.setDistance_m(a.getDistance());
                    activityRun.setAvg_heart_rate_bpm(a.getAverageHeartRate());
                    activityRun.setMax_heart_rate_bpm(a.getMaxHeartRate());
                    activityRun.setDate(a.getStartDateLocal().toLocalDate() + "");
                    activityRun.setTotal_elev_gain_m(a.getTotalElevationGain());
                    activityRun.setStrava_activity_id(a.getStravaActivityId());
                    activityRun.setDescription(a.getDescription() + (a.getDescriptionTyped() == null ? "" : " " + a.getDescriptionTyped()));

                    actionsRun.getActivity().add(activityRun);
                    break;
                case "WeightTraining":
                    activityWeightTraining.setElapsed_time_in_sec(a.getElapsedTime());
                    activityWeightTraining.setAvg_heart_rate_bpm(a.getAverageHeartRate());
                    activityWeightTraining.setMax_heart_rate_bpm(a.getMaxHeartRate());
                    activityWeightTraining.setDate(a.getStartDateLocal().toLocalDate() + "");
                    activityWeightTraining.setStrava_activity_id(a.getStravaActivityId());
                    activityWeightTraining.setDescription(a.getDescription() + (a.getDescriptionTyped() == null ? "" : " " + a.getDescriptionTyped()));
                    actionsWeightTraining.getActivity().add(activityWeightTraining);
                    break;
                default:
                    activityRest.setElapsed_time_in_sec(a.getElapsedTime());
                    activityRest.setAvg_heart_rate_bpm(a.getAverageHeartRate());
                    activityRest.setMax_heart_rate_bpm(a.getMaxHeartRate());
                    activityRest.setDate(a.getStartDateLocal().toLocalDate() + "");
                    activityRest.setDistance_m(a.getDistance());
                    activityRest.setType_activity(a.getType());
                    activityRest.setStrava_activity_id(a.getStravaActivityId());
                    activityRest.setDescription(a.getDescription() + (a.getDescriptionTyped() == null ? "" : " " + a.getDescriptionTyped()));
                    actionsRest.getActivity().add(activityRest);
            }
        }
        activitiesToPromptDto.setActivity_ride_bike(actionsBike);
        activitiesToPromptDto.setActivity_run(actionsRun);
        activitiesToPromptDto.setActivity_weight_training(actionsWeightTraining);
        activitiesToPromptDto.setActivity_rest(actionsRest);

        ReadyToSendAi readyToSendAi = new ReadyToSendAi();

        readyToSendAi.setActivities(activitiesToPromptDto);
        readyToSendAi.getLimitations().add("average_speed based on moving_time");
        readyToSendAi.getLimitations().add("power is average power");
        readyToSendAi.getLimitations().add("normalized power is in some activities");
        readyToSendAi.getLimitations().add("null means missing sensor data, not zero");
        readyToSendAi.setAthleteContext(new ReadyToSendAi.AthleteContext());

        return readyToSendAi;
    }

}

