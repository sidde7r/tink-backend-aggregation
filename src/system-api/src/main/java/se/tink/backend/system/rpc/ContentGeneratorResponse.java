package se.tink.backend.system.rpc;

import java.util.List;

import se.tink.backend.core.Activity;
import se.tink.backend.core.Statistic;

public class ContentGeneratorResponse {

    private List<Activity> activities;
    private List<Statistic> statistics;

    public List<Activity> getActivities() {
        return activities;
    }

    public List<Statistic> getStatistics() {
        return statistics;
    }

    public void setActivities(List<Activity> activities) {
        this.activities = activities;
    }

    public void setStatistics(List<Statistic> statistics) {
        this.statistics = statistics;
    }

}
