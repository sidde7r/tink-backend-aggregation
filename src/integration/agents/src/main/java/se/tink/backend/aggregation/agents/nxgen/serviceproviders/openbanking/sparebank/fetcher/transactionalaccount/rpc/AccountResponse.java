package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.transactionalaccount.rpc;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@JsonObject
public class AccountResponse {
    private List<AccountEntity> accounts;

    public AccountResponse() {}

    public AccountResponse(final List<AccountEntity> accounts) {
        this.accounts = accounts;
    }

    public void setAccounts(List<AccountEntity> accounts) {
        this.accounts = accounts;
    }

    public List<AccountEntity> getAccounts() {
        return accounts;
    }

    public Collection<TransactionalAccount> toTinkAccounts() {
        return Optional.ofNullable(accounts).orElse(Collections.emptyList()).stream()
                .map(AccountEntity::toTinkAccount)
                .collect(Collectors.toList());
    }
}
