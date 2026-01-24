package com.example.training.model;

public class ActivitiesToPromptDto {
    private AcitivityBase<ActivityBike> activity_ride_bike;
    private AcitivityBase<ActivityRun> activity_run;
    private AcitivityBase<ActivityWeightTraining> activity_weight_training;
    private AcitivityBase<ActivityRest> activity_rest;


    public AcitivityBase<ActivityBike> getActivity_ride_bike() {
        return activity_ride_bike;
    }

    public void setActivity_ride_bike(AcitivityBase<ActivityBike> activity_ride_bike) {
        this.activity_ride_bike = activity_ride_bike;
    }

    public AcitivityBase<ActivityWeightTraining> getActivity_weight_training() {
        return activity_weight_training;
    }

    public void setActivity_weight_training(AcitivityBase<ActivityWeightTraining> activity_weight_training) {
        this.activity_weight_training = activity_weight_training;
    }

    public AcitivityBase<ActivityRest> getActivity_rest() {
        return activity_rest;
    }

    public void setActivity_rest(AcitivityBase<ActivityRest> activity_rest) {
        this.activity_rest = activity_rest;
    }

    public AcitivityBase<ActivityRun> getActivity_run() {
        return activity_run;
    }

    public void setActivity_run(AcitivityBase<ActivityRun> activity_run) {
        this.activity_run = activity_run;
    }
}
