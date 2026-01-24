package com.example.training.model;

public class Prompt {
    private String prompt;
    private FilterActivityType filterActivityType;

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public FilterActivityType getFilterActivityType() {
        return filterActivityType;
    }

    public void setFilterActivityType(FilterActivityType filterActivityType) {
        this.filterActivityType = filterActivityType;
    }
}
