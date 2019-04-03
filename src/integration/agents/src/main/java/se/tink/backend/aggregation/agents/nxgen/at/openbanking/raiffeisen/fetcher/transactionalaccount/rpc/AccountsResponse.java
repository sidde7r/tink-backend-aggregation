package se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.fetcher.transactionalaccount.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.fetcher.transactionalaccount.entity.account.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountsResponse {

    private List<AccountEntity> accounts;

    public List<AccountEntity> getAccounts() {
        return accounts;
    }
}
