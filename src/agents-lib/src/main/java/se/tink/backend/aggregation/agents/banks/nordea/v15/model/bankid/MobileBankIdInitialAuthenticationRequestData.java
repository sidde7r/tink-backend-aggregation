package se.tink.backend.aggregation.agents.banks.nordea.v15.model.bankid;

import java.util.HashMap;
import java.util.Map;

public class MobileBankIdInitialAuthenticationRequestData {
    private Map<String, Object> userId = new HashMap<String, Object>();

    public Map<String, Object> getUserId() {
        return userId;
    }

    public void setUserId(Map<String, Object> userId) {
        this.userId = userId;
    }
}
