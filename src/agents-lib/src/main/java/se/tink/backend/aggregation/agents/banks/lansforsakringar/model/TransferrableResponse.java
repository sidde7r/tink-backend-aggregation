package se.tink.backend.aggregation.agents.banks.lansforsakringar.model;

import java.util.List;

public class TransferrableResponse {
    private List<AccountEntity> accounts;

    public List<AccountEntity> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<AccountEntity> accounts) {
        this.accounts = accounts;
    }
}
