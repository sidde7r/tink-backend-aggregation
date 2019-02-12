package se.tink.backend.aggregation.agents.abnamro.client.rpc;

import java.util.List;

public class SubscriptionAccountsRequest {
    private String bcNumber;
    private List<Long> contracts;

    public String getBcNumber() {
        return bcNumber;
    }

    public void setBcNumber(String bcNumber) {
        this.bcNumber = bcNumber;
    }

    public List<Long> getContracts() {
        return contracts;
    }

    public void setContracts(List<Long> contracts) {
        this.contracts = contracts;
    }
}
