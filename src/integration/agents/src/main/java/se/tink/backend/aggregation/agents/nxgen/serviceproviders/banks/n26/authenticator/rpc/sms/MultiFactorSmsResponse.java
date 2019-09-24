package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.authenticator.rpc.sms;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class MultiFactorSmsResponse {
    private String challengeType;
    private String remainingResendCodeCount;
    private String waitingTimeInSeconds;
    private String obfuscatedPhoneNumber;

    public String getChallengeType() {
        return challengeType;
    }

    public String getRemainingResendCodeCount() {
        return remainingResendCodeCount;
    }

    public String getWaitingTimeInSeconds() {
        return waitingTimeInSeconds;
    }

    public String getObfuscatedPhoneNumber() {
        return obfuscatedPhoneNumber;
    }
}
