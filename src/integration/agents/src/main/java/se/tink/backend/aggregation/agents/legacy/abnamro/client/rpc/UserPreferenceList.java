package se.tink.backend.aggregation.agents.abnamro.client.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserPreferenceList {
    private List<UserPreferences> userPreferences;

    public List<UserPreferences> getUserPreferences() {
        return userPreferences;
    }

    public void setUserPreferences(List<UserPreferences> userPreferences) {
        this.userPreferences = userPreferences;
    }
}
