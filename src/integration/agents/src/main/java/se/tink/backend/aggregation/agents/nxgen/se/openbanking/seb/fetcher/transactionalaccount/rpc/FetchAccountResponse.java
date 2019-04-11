package se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.fetcher.transactionalaccount.rpc;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.fetcher.transactionalaccount.entities.AccountsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@JsonObject
public class FetchAccountResponse {

    private List<AccountsEntity> accounts;

    public Collection<TransactionalAccount> toTinkAccounts() {
        return accounts != null
                ? accounts.stream()
                        .filter(AccountsEntity::isEnabled)
                        .map(AccountsEntity::toTinkAccount)
                        .collect(Collectors.toList())
                : Collections.emptyList();
    }
}
