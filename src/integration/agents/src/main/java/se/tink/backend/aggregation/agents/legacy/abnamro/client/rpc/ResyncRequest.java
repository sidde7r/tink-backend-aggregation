package se.tink.backend.aggregation.agents.abnamro.client.rpc;

import java.util.List;

public class ResyncRequest {

    private List<Long> accounts;

    public List<Long> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<Long> accounts) {
        this.accounts = accounts;
    }
}
