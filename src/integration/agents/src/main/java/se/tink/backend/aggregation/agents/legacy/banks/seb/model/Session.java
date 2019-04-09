package se.tink.backend.aggregation.agents.banks.seb.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import se.tink.backend.aggregation.utils.CookieContainer;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Session extends CookieContainer {

    private String customerId;

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }
}
