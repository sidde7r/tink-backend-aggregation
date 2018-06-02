package se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InterestDurationEntity {

    private List<InterestDuration> durations;

    public List<InterestDuration> getDurations() {
        return durations;
    }

    public void setDurations(List<InterestDuration> durations) {
        this.durations = durations;
    }
}
