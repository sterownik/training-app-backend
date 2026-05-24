package com.example.training.model;

import java.util.ArrayList;

public class ReadyToSendAi {
    private final String source = "strava";
    private final ArrayList<String> limitations = new ArrayList<>();
    private AthleteContext athleteContext;
    private ActivitiesToPromptDto activities;


    public ActivitiesToPromptDto getActivities() {
        return activities;
    }

    public void setActivities(ActivitiesToPromptDto activities) {
        this.activities = activities;
    }

    public AthleteContext getAthleteContext() {
        return athleteContext;
    }

    public void setAthleteContext(AthleteContext athleteContext) {
        this.athleteContext = athleteContext;
    }

    public static class AthleteContext {
        public final String sport_background = "cycling + running";
        public final String goal =  "general fitness / FTP improvement";
        public final String experience_level = "próg przemian tlenowych 200 watt 168 bpm, beztletnowych 250watt 189 bpm" +
                ", hemoglobina 16,7 g/dl, hemokryt 48%, vo2max 55, waga 67kg";

        public AthleteContext() {
        }

        public String getSport_background() {
            return sport_background;
        }

        public String getGoal() {
            return goal;
        }

        public String getExperience_level() {
            return experience_level;
        }
    }

    public String getSource() {
        return source;
    }

    public ArrayList<String> getLimitations() {
        return limitations;
    }
}
