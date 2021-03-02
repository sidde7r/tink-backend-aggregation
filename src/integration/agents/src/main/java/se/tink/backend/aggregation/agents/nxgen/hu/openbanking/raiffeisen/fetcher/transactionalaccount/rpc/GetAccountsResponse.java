package se.tink.backend.aggregation.agents.nxgen.hu.openbanking.raiffeisen.fetcher.transactionalaccount.rpc;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.hu.openbanking.raiffeisen.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@JsonObject
public class GetAccountsResponse {

    private List<AccountEntity> accounts;

    public Collection<TransactionalAccount> toTinkAccounts() {
        return Optional.ofNullable(accounts).orElse(Collections.emptyList()).stream()
                .filter(AccountEntity::isCheckingAccount)
                .map(AccountEntity::toTinkAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
