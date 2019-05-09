package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.fetcher.transactionalaccount.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.fetcher.transactionalaccount.entities.accounts.AccountsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountsResponse {
    private List<AccountsEntity> accounts;

    public List<AccountsEntity> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<AccountsEntity> accounts) {
        this.accounts = accounts;
    }
}
