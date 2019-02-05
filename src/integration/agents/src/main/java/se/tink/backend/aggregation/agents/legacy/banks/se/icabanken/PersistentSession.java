package se.tink.backend.aggregation.agents.banks.se.icabanken;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import se.tink.backend.aggregation.utils.CookieContainer;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PersistentSession extends CookieContainer {
    private String sessionId;
    private String userInstallationId;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getUserInstallationId() {
        return userInstallationId;
    }

    public void setUserInstallationId(String userInstallationId) {
        this.userInstallationId = userInstallationId;
    }
}
