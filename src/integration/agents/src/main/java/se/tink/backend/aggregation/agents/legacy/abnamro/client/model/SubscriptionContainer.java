package se.tink.backend.aggregation.agents.abnamro.client.model;

import java.util.List;

public class SubscriptionContainer {

    private List<Long> accounts;
    private String bcNumber;

    public List<Long> getAccounts() {
        return accounts;
    }

    public String getBcNumber() {
        return bcNumber;
    }

    public void setAccounts(List<Long> accounts) {
        this.accounts = accounts;
    }

    public void setBcNumber(String bcNumber) {
        this.bcNumber = bcNumber;
    }
}
