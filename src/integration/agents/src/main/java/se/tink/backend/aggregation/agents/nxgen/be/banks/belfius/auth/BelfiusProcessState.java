package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import se.tink.backend.aggregation.annotations.JsonObject;

@NoArgsConstructor
@AllArgsConstructor
@JsonObject
public class BelfiusProcessState {

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

    public BelfiusProcessState deviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
        return this;
    }

    public BelfiusProcessState contractNumber(String contractNumber) {
        this.contractNumber = contractNumber;
        return this;
    }

    public BelfiusProcessState challenge(String challenge) {
        this.challenge = challenge;
        return this;
    }

    public BelfiusProcessState encryptedPassword(String encryptedPassword) {
        this.encryptedPassword = encryptedPassword;
        return this;
    }

    public BelfiusProcessState deviceTokenHashed(String deviceTokenHashed) {
        this.deviceTokenHashed = deviceTokenHashed;
        return this;
    }

    public BelfiusProcessState deviceTokenHashedIosComparison(
            String deviceTokenHashedIosComparison) {
        this.deviceTokenHashedIosComparison = deviceTokenHashedIosComparison;
        return this;
    }

    public BelfiusProcessState sessionId(String sessionId) {
        this.sessionId = sessionId;
        return this;
    }

    public BelfiusProcessState machineId(String machineId) {
        this.machineId = machineId;
        return this;
    }
}
