package com.example.training.model;

import java.util.ArrayList;

public class AcitivityBase<T> {
    private ArrayList<T> activity = new ArrayList<T>();
    private String description;
    private String type;

    public ArrayList<T> getActivity() {
        return activity;
    }

    public void setActivity(ArrayList<T> activity) {
        this.activity = activity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
