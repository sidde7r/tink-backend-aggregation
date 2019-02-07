package se.tink.backend.aggregation.agents.nxgen.es.banks.popular.fetcher.rpc;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.entities.BancoPopularResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.fetcher.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.fetcher.entities.AccountsWrapperEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@JsonObject
public class FetchAccountsResponse extends BancoPopularResponse {
    private AccountsWrapperEntity custom;

    public Collection<TransactionalAccount> getTinkAccounts() {
        if (custom != null && custom.getAccountList() != null) {
            return custom.getAccountList().stream()
                    .map(AccountEntity::toTinkAccount)
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    public AccountsWrapperEntity getCustom() {
        return custom;
    }
}
