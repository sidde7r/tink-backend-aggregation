package se.tink.backend.aggregation.agents.legacy.banks.seb.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import se.tink.backend.aggregation.utils.CookieContainer;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Session extends CookieContainer {

    private String customerId;
    private String userName;
    private String userId;

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
