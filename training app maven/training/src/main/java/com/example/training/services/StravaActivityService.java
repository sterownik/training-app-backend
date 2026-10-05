package com.example.training.services;

import com.example.training.model.*;
import com.example.training.repository.ActivityRepository;
import com.example.training.repository.UserRepository;
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
    private final UserRepository userRepository;
    private final OpenAiService openAiService;

    public StravaActivityService(ActivityRepository activityRepository, UserRepository userRepository, OpenAiService openAiService) {
        this.activityRepository = activityRepository;
        this.userRepository = userRepository;
        this.openAiService = openAiService;
    }

    // Szczegoly aktywnosci zwracaja jednoczesnie okrazenia i zdjecia, wiec jedno zapytanie na aktywnosc
    public void updateDetails(String accessToken, User user) throws InterruptedException {
        List<Activity> activities = activityRepository
                .findFirst20ByUserIdOrderByStartDateLocalDesc(user.getId());

        for (Activity activity : activities) {
            boolean needsLaps = activity.getLaps() == null
                    && (activity.getType().contains("Ride") || activity.getType().contains("Run"));
            boolean needsPhoto = activity.getPhotoUrl() == null
                    && activity.getPhotoCount() != null && activity.getPhotoCount() > 0;

            if (needsLaps || needsPhoto) {
                try {
                    String url = "https://www.strava.com/api/v3/activities/" + activity.getStravaActivityId();

                    HttpHeaders headers = new HttpHeaders();
                    headers.setBearerAuth(accessToken);

                    HttpEntity<String> entity = new HttpEntity<>(headers);

                    ResponseEntity<StravaActivity> response = restTemplate.exchange(
                            url,
                            HttpMethod.GET,
                            entity,
                            StravaActivity.class
                    );

                    StravaActivity details = response.getBody();
                    if (details == null) {
                        continue;
                    }

                    boolean changed = false;
                    String photoUrl = primaryPhotoUrl(details);
                    if (photoUrl != null) {
                        activity.setPhotoUrl(photoUrl);
                        changed = true;
                    }

                    StravaLapDto[] lapsStrava = details.getLaps();
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
                        changed = true;
                    }

                    // Opis interwalow tylko przy pierwszym pobraniu okrazen i gdy uzytkownik nie ma wlasnego opisu
                    boolean hasOwnDescription = activity.getDescriptionTyped() != null
                            && !activity.getDescriptionTyped().isBlank();
                    if (needsLaps && !hasOwnDescription) {
                        String intervals = describeIntervals(lapsStrava);
                        if (intervals != null) {
                            activity.setDescriptionTyped(intervals);
                            changed = true;
                        }
                    }
                    if (changed) {
                        activityRepository.save(activity);
                    }
                    Thread.sleep(100);
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
        // Trasy uzupelniamy tylko dla najnowszych aktywnosci, zeby synchronizacja byla szybka
        List<Activity> newest = activityRepository.findFirst50ByUserIdOrderByStartDateLocalDesc(user.getId());
        Optional<OffsetDateTime> oldestWithoutMap = newest.stream()
                .filter(activity -> activity.getSummaryPolyline() == null)
                .map(Activity::getStartDateLocal)
                .min(Comparator.naturalOrder());

        long before = Instant.now().getEpochSecond();
        // start_date_local to czas lokalny zapisany jako UTC, zapas 1 dnia zeby nie pominac aktywnosci
        long after = oldestWithoutMap
                .map(d -> d.toInstant().getEpochSecond() - 86400)
                .or(() -> latest.map(d -> d.toInstant().getEpochSecond()))
                .orElseGet(() ->
                        Instant.now()
                                .minus(200, ChronoUnit.DAYS)
                                .getEpochSecond()
                );

        List<Activity> result = new ArrayList<>();
        Set<Long> updatedStravaIds = new HashSet<>();
        boolean completed = true;

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

            ResponseEntity<StravaActivity[]> response;
            try {
                response = restTemplate.exchange(
                        url,
                        HttpMethod.GET,
                        entity,
                        StravaActivity[].class
                );
            } catch (HttpClientErrorException.TooManyRequests e) {
                // limit Stravy - zapisz to, co juz pobrane, reszta przy nastepnej synchronizacji
                System.err.println("Przekroczono limit Stravy (429) przy pobieraniu listy aktywności.");
                completed = false;
                break;
            }

            StravaActivity[] activities = response.getBody();

            if (activities == null || activities.length == 0) {
                break;
            }

            // jedno zapytanie do bazy na strone zamiast jednego na aktywnosc
            List<Long> stravaIds = Arrays.stream(activities).map(StravaActivity::getId).toList();
            Map<Long, Activity> existingByStravaId = new HashMap<>();
            activityRepository.findByStravaActivityIdIn(stravaIds)
                    .forEach(activity -> existingByStravaId.put(activity.getStravaActivityId(), activity));

            for (StravaActivity a : activities) {
                Activity existingActivity = existingByStravaId.get(a.getId());
                if (existingActivity != null) {
                    if (existingActivity.getSummaryPolyline() == null) {
                        existingActivity.setSummaryPolyline(summaryPolylineOf(a));
                        existingActivity.setPhotoCount(a.getTotalPhotoCount());
                        result.add(existingActivity);
                        updatedStravaIds.add(a.getId());
                    }
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
                activityDto.setPhotoUrl(primaryPhotoUrl(a));
                activityDto.setPhotoCount(a.getTotalPhotoCount());
                activityDto.setSummaryPolyline(summaryPolylineOf(a));

                activityDto.setDescription(a.getName());



                result.add(activityDto);
            }

            page++;
        }
        activityRepository.saveAll(result);

        if (completed) {
            // Aktywnosci, ktorych Strava juz nie zwraca (np. usuniete) - oznacz, zeby nie pobierac ich w kolko
            List<Activity> stillWithoutMap = newest.stream()
                    .filter(activity -> activity.getSummaryPolyline() == null)
                    .filter(activity -> !updatedStravaIds.contains(activity.getStravaActivityId()))
                    .toList();
            stillWithoutMap.forEach(activity -> activity.setSummaryPolyline(""));
            activityRepository.saveAll(stillWithoutMap);
        }
    }

    // Okrazenie ze srednia moca powyzej tego progu traktujemy jako interwal
    static final int INTERVAL_POWER_THRESHOLD_WATTS = 220;

    /**
     * Np. "10 min interwał 240 W, tętno 165 bpm; 5 min przerwy; 10 min interwał 245 W, tętno 170 bpm".
     * Zwraca null, gdy zadne okrazenie nie przekracza progu mocy.
     */
    static String describeIntervals(StravaLapDto[] laps) {
        if (laps == null) {
            return null;
        }

        List<String> parts = new ArrayList<>();
        int restSeconds = 0;
        boolean afterInterval = false;

        for (StravaLapDto lap : laps) {
            boolean isInterval = lap.getAverageWatts() != null
                    && lap.getAverageWatts() > INTERVAL_POWER_THRESHOLD_WATTS;

            if (!isInterval) {
                // przerwe liczymy tylko miedzy interwalami (bez rozgrzewki i schlodzenia)
                if (afterInterval) {
                    restSeconds += lapSeconds(lap.getElapsedTime(), lap.getMovingTime());
                }
                continue;
            }

            if (afterInterval && restSeconds > 0) {
                parts.add(formatDuration(restSeconds) + " przerwy");
            }
            restSeconds = 0;
            afterInterval = true;

            StringBuilder interval = new StringBuilder()
                    .append(formatDuration(lapSeconds(lap.getMovingTime(), lap.getElapsedTime())))
                    .append(" interwał ")
                    .append(lap.getAverageWatts())
                    .append(" W");
            if (lap.getAverageHeartrate() != null) {
                interval.append(", tętno ").append(Math.round(lap.getAverageHeartrate())).append(" bpm");
            }
            parts.add(interval.toString());
        }

        if (parts.isEmpty()) {
            return null;
        }
        String description = String.join("; ", parts);
        return description.length() > 2000 ? description.substring(0, 2000) : description;
    }

    private static int lapSeconds(Integer preferred, Integer fallback) {
        if (preferred != null) {
            return preferred;
        }
        return fallback != null ? fallback : 0;
    }

    // ponizej minuty dokladnie, powyzej zaokraglone do 10 s: "45 s", "10 min", "4 min 30 s"
    static String formatDuration(int seconds) {
        if (seconds < 60) {
            return seconds + " s";
        }
        int rounded = (int) Math.round(seconds / 10.0) * 10;
        int minutes = rounded / 60;
        int rest = rounded % 60;
        return rest == 0 ? minutes + " min" : minutes + " min " + rest + " s";
    }

    private static String summaryPolylineOf(StravaActivity a) {
        if (a.getMap() == null || a.getMap().getSummaryPolyline() == null) {
            return "";
        }
        return a.getMap().getSummaryPolyline();
    }

    private static String primaryPhotoUrl(StravaActivity a) {
        if (a.getPhotos() == null || a.getPhotos().getPrimary() == null
                || a.getPhotos().getPrimary().getUrls() == null) {
            return null;
        }
        return a.getPhotos().getPrimary().getUrls().get("600");
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

    public ReadyToSendAi prepareData(User user
// FilterActivityType filterActivityType
    ) {
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
//        System.out.println(filterActivityType.getFilterType());
//        if(filterActivityType.getFilterType().contains("date")) {
//            activities = activityRepository.findByUserIdAndStartByStartDateLocalAndEnd(user.getId(), filterActivityType.getStartDateLocalStart(), filterActivityType.getStartDateLocalEnd());
//        }
//        else if(filterActivityType.getActivityIds() != null) {
//            activities = activityRepository.findByUserIdAndIdInOrderByStartDateLocalDesc(user.getId(), filterActivityType.getActivityIds());
//        }
//        else {

            activities = activityRepository.findFirst10ByUserIdOrderByStartDateLocalDesc(user.getId());
//        }

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
//                    activityBike.setMax_heart_rate_bpm(a.getMaxHeartRate());
                    activityBike.setDate(a.getStartDateLocal().toLocalDate() + "");
                    activityBike.setAvg_watts(a.getAverageWatts());
                    activityBike.setAvg_speed_km_h(calculateAverageSpeed(a.getDistance(), a.getMoving_time()));
//                    activityBike.setMax_speed_km_h(convertMsToKmh(a.getMaxSpeed()));
                    activityBike.setTotal_elevation_gain_m(a.getTotalElevationGain());
                    activityBike.setStrava_activity_id(a.getStravaActivityId());
                    activityBike.setNormalized_power(a.getNormalizedPower());
                    activityBike.setDescription(a.getDescription() + " " + (a.getLaps() == null ? "" : " " + "okrążenia " +a.getLaps())+ ""+ (a.getDescriptionTyped() == null ? "" : " " + a.getDescriptionTyped()));
                    actionsBike.getActivity().add(activityBike);
                    break;

                case "Run":
                    activityRun.setAvg_pace_min_per_km(paceFromDistanceAndMovingTime(a.getDistance(), a.getMoving_time()));
                    activityRun.setElapsed_time_in_sec(a.getElapsedTime());
                    activityRun.setDistance_m(a.getDistance());
                    activityRun.setAvg_heart_rate_bpm(a.getAverageHeartRate());
//                    activityRun.setMax_heart_rate_bpm(a.getMaxHeartRate());
                    activityRun.setDate(a.getStartDateLocal().toLocalDate() + "");
                    activityRun.setTotal_elev_gain_m(a.getTotalElevationGain());
                    activityRun.setStrava_activity_id(a.getStravaActivityId());
                    activityRun.setDescription(a.getDescription() + " " + (a.getLaps() == null ? "" : " " + "okrążenia " +a.getLaps())+ "" + (a.getDescriptionTyped() == null ? "" : " " + a.getDescriptionTyped()));

                    actionsRun.getActivity().add(activityRun);
                    break;
                case "WeightTraining":
                    activityWeightTraining.setElapsed_time_in_sec(a.getElapsedTime());
                    activityWeightTraining.setAvg_heart_rate_bpm(a.getAverageHeartRate());
//                    activityWeightTraining.setMax_heart_rate_bpm(a.getMaxHeartRate());
                    activityWeightTraining.setDate(a.getStartDateLocal().toLocalDate() + "");
                    activityWeightTraining.setStrava_activity_id(a.getStravaActivityId());
                    activityWeightTraining.setDescription(a.getDescription() + (a.getDescriptionTyped() == null ? "" : " " + a.getDescriptionTyped()));
                    actionsWeightTraining.getActivity().add(activityWeightTraining);
                    break;
                default:
                    activityRest.setElapsed_time_in_sec(a.getElapsedTime());
                    activityRest.setAvg_heart_rate_bpm(a.getAverageHeartRate());
//                    activityRest.setMax_heart_rate_bpm(a.getMaxHeartRate());
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

        readyToSendAi.setAthleteContext(
                userRepository.findById(user.getId())
                        .map(User::getAthleteInfo)
                        .orElse("")
        );

        return readyToSendAi;
    }

}

