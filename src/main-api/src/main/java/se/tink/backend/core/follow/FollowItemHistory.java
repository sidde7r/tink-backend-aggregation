package se.tink.backend.core.follow;

import java.util.Collections;
import java.util.Map;
import se.tink.libraries.date.Period;

public class FollowItemHistory {
    private Map<Period, Double> statistics;

    public Map<Period, Double> getStatistics() {
        return statistics == null ? Collections.emptyMap() : statistics;
    }

    public void setStatistics(Map<Period, Double> statistics) {
        this.statistics = statistics;
    }
}
