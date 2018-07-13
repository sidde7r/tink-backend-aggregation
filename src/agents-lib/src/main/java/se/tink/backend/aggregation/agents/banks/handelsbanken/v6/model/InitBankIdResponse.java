package se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InitBankIdResponse extends AbstractResponse {
    private String autoStartToken;
    private int initialSleepTime;

    public String getAutoStartToken() {
        return autoStartToken;
    }

    public void setAutoStartToken(String autoStartToken) {
        this.autoStartToken = autoStartToken;
    }

    public int getInitialSleepTime() {
        return initialSleepTime;
    }

    public void setInitialSleepTime(int initialSleepTime) {
        this.initialSleepTime = initialSleepTime;
    }

}
