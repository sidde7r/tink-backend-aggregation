package se.tink.backend.aggregation.agents.framework.assertions.entities;

import java.util.List;
import java.util.Map;

public class AgentContractEntity {

    private List<Map<String, Object>> accounts;

    private List<Map<String, Object>> transactions;

    private Map<String, Object> identityData;

    public List<Map<String, Object>> getAccounts() {
        return accounts;
    }

    public List<Map<String, Object>> getTransactions() {
        return transactions;
    }

    public Map<String, Object> getIdentityData() {
        return identityData;
    }
}
