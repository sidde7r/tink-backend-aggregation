package se.tink.backend.aggregation.agents.banks.skandiabanken.model;

import se.tink.backend.aggregation.utils.CookieContainer;

public class PersistentSession extends CookieContainer {
    private Integer customerId;

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public Integer getCustomerId() {
        return customerId;
    }
}
