package se.tink.backend.aggregation.agents.abnamro.client.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserPreferenceResponse extends ErrorResponse {
    private UserPreferenceList userPreferenceList;

    public UserPreferenceList getUserPreferenceList() {
        return userPreferenceList;
    }

    public void setUserPreferenceList(UserPreferenceList userPreferenceList) {
        this.userPreferenceList = userPreferenceList;
    }

    public boolean isError() {
        return getMessages() != null && !getMessages().isEmpty();
    }
}
