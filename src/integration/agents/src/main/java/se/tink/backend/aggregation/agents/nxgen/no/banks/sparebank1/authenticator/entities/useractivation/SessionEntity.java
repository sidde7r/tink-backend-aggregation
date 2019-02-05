package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.entities.useractivation;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SessionEntity {
    private Integer maxInactiveInterval;
    private Integer remainingSessionTime;

    public Integer getMaxInactiveInterval() {
        return maxInactiveInterval;
    }

    public Integer getRemainingSessionTime() {
        return remainingSessionTime;
    }
}
