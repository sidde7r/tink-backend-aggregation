package se.tink.backend.aggregation.aggregationcontroller.v1.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import se.tink.libraries.enums.StatisticGenerationMode;
import se.tink.libraries.enums.StatisticMode;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GenerateStatisticsAndActivitiesRequest {
    private String credentialsId;
    private StatisticMode mode;
    private boolean userTriggered = false;
    private boolean takeReadlock = true;

    private String userId;
    private StatisticGenerationMode statisticGenerationMode = StatisticGenerationMode.REWRITE;

    public String getCredentialsId() {
        return credentialsId;
    }

    public StatisticMode getMode() {
        return mode;
    }

    public String getUserId() {
        return userId;
    }

    public boolean isUserTriggered() {
        return userTriggered;
    }

    public void setCredentialsId(String credentialsId) {
        this.credentialsId = credentialsId;
    }

    public void setMode(StatisticMode mode) {
        this.mode = mode;
    }

    public void setUserTriggered(boolean userTriggered) {
        this.userTriggered = userTriggered;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isTakeReadlock() {
        return takeReadlock;
    }

    public void setTakeReadlock(boolean takeReadlock) {
        this.takeReadlock = takeReadlock;
    }

    public StatisticGenerationMode getStatisticGenerationMode() {
        return statisticGenerationMode;
    }

    public void setStatisticGenerationMode(StatisticGenerationMode statisticGenerationMode) {
        this.statisticGenerationMode = statisticGenerationMode;
    }
}
