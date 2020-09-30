package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
public class BelfiusProcessState {

    public static final String KEY = BelfiusProcessState.class.getName();

    @Getter @Setter private String deviceToken;
    @Getter @Setter private String contractNumber;
    @Getter @Setter private String challenge;
    @Getter @Setter private String encryptedPassword;
    @Getter @Setter private String deviceTokenHashed;
    @Getter @Setter private String deviceTokenHashedIosComparison;
    @Getter @Setter private String sessionId;
    @Getter @Setter private String machineId;
    @Getter private int requestCounterAggregated;
    @Getter private int requestCounterServices;

    public String incrementAndGetRequestCounterAggregated() {
        requestCounterAggregated++;
        return String.valueOf(requestCounterAggregated);
    }

    public String incrementAndGetRequestCounterServices() {
        requestCounterServices++;
        return String.valueOf(requestCounterServices);
    }

    public void resetRequestCounterAggregated() {
        requestCounterAggregated = 0;
    }

    public void resetRequestCounterServices() {
        requestCounterServices = 0;
    }
}
