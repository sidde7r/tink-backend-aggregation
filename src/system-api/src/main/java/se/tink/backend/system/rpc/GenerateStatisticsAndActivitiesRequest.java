package se.tink.backend.system.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Optional;

import se.tink.backend.core.StatisticGenerationMode;
import se.tink.backend.core.StatisticMode;
import se.tink.backend.core.UserData;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GenerateStatisticsAndActivitiesRequest {
    private String credentialsId;
    private StatisticMode mode;
    private boolean userTriggered = false;
    private boolean takeReadlock = true;

    @JsonIgnore
    private Optional<UserData> userData = Optional.empty();

    private String userId;
    private StatisticGenerationMode statisticGenerationMode = StatisticGenerationMode.REWRITE;
    public String getCredentialsId() {
        return credentialsId;
    }

    public StatisticMode getMode() {
        return mode;
    }

    /**
     * Get the user data. Returns an {@link Optional}, since Java might have garbage collected this.
     * 
     * @return an optional user data data structure.
     */
    public Optional<UserData> getUserData() {
        return userData;
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

    /**
     * Store a {@link UserData} instance softly. Note that this might be garbage collected at any time.
     * 
     * @param userData
     *            the data to store
     */
    public void setUserData(UserData userData) {
        this.userData = Optional.of(userData);
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
