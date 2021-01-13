package se.tink.backend.aggregation.agents.framework.assertions.entities;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AgentContractEntity {

    private List<Map<String, Object>> accounts;

    private List<Map<String, Object>> transactions;

    private Map<String, Object> identityData;

    private List<Map<String, Object>> transferDestinationPatterns;

    public List<Map<String, Object>> getAccounts() {
        return accounts;
    }

    public List<Map<String, Object>> getTransactions() {
        return transactions;
    }

    public Optional<Map<String, Object>> getIdentityData() {
        return Optional.ofNullable(identityData);
    }

    public List<Map<String, Object>> getTransferDestinationPatterns() {
        return transferDestinationPatterns;
    }
}
