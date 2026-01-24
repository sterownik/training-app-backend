package com.example.training.model;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;

public class FilterActivityType {
    private String filterType;
    private Optional<ArrayList<Long>> activityIds;
    private Date startDateLocalStart;
    private Date startDateLocalEnd;

    public Date getStartDateLocalStart() {
        return startDateLocalStart;
    }

    public void setStartDateLocalStart(Date startDateLocalStart) {
        this.startDateLocalStart = startDateLocalStart;
    }

    public Date getStartDateLocalEnd() {
        return startDateLocalEnd;
    }

    public void setStartDateLocalEnd(Date startDateLocalEnd) {
        this.startDateLocalEnd = startDateLocalEnd;
    }

    public Optional<ArrayList<Long>> getActivityIds() {
        return activityIds;
    }

    public void setActivityIds(Optional<ArrayList<Long>> activityIds) {
        this.activityIds = activityIds;
    }

    public String getFilterType() {
        return filterType;
    }

    public void setFilterType(String filterType) {
        this.filterType = filterType;
    }
}
