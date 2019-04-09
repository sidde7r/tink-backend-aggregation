package se.tink.backend.aggregation.agents.banks.nordea;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.joda.time.DateTime;
import se.tink.backend.aggregation.utils.CookieContainer;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Session extends CookieContainer {
    private String securityToken;
    private DateTime lastUpdated;

    public String getSecurityToken() {
        return securityToken;
    }

    public void setSecurityToken(String securityToken) {
        this.securityToken = securityToken;
    }

    public boolean isExpired() {
        // Don't load stored session if it is older than 30 minutes. This is because Nordea has a
        // timeout
        // and a client cannot be logged in for more than 30 minutes.
        return lastUpdated != null && lastUpdated.plusMinutes(30).isBefore(DateTime.now());
    }

    public void setLastUpdated(DateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
