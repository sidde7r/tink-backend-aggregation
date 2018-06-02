package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.entities.useractivation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SessionEntity {
    private Integer maxInactiveInterval;
    private Integer remainingSessionTime;

    public Integer getMaxInactiveInterval() {
        return maxInactiveInterval;
    }

    public void setMaxInactiveInterval(Integer maxInactiveInterval) {
        this.maxInactiveInterval = maxInactiveInterval;
    }

    public Integer getRemainingSessionTime() {
        return remainingSessionTime;
    }

    public void setRemainingSessionTime(Integer remainingSessionTime) {
        this.remainingSessionTime = remainingSessionTime;
    }
}
