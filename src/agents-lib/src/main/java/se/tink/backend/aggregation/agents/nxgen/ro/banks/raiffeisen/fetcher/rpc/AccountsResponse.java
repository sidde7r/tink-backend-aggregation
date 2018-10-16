package se.tink.backend.aggregation.agents.nxgen.ro.banks.raiffeisen.fetcher.rpc;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.ro.banks.raiffeisen.fetcher.entity.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;

@JsonObject
public class AccountsResponse {
    private List<AccountEntity> accounts;

    public Collection<TransactionalAccount> toTransactionalAccounts() {
        return accounts.stream().map(AccountEntity::toTransactionalAccount)
                .collect(Collectors.toList());
    }
}
