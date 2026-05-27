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
        public final String experience_level = "SPORTOWIEC:\n" +
                "- wiek: 30\n" +
                "- wzrost: 173 cm\n" +
                "- masa: 68 kg\n" +
                "\n" +
                "WYDOLNOŚĆ:\n" +
                "- VO2max: 55.2 ml/kg/min\n" +
                "- FTP/prog anaerobowy (AT): 250 W\n" +
                "- FTP względne: 3.68 W/kg\n" +
                "- próg tlenowy (LT): 200 W\n" +
                "- moc maksymalna: 320 W\n" +
                "- HR max: 205\n" +
                "\n" +
                "STREFY:\n" +
                "- recovery: <150 W\n" +
                "- low intensity: 150–200 W\n" +
                "- middle intensity: 200–225 W\n" +
                "- high intensity: 225–250 W\n" +
                "- very high intensity: >250 W\n" +
                "\n" +
                "CHARAKTERYSTYKA:\n" +
                "- dobra wydolność tlenowa\n" +
                "- dobry potencjał wysiłków wytrzymałościowych\n" +
                "- mocny metabolizm beztlenowy\n" +
                "- dobra regeneracja mleczanu\n" +
                "- cel: poprawa FTP";

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
