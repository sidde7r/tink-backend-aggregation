package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.rpc;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.entities.AccountBaseEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@JsonObject
public class AccountsBaseResponse implements AccountResponseToTink {
    protected List<AccountBaseEntity> accounts;

    public AccountsBaseResponse(final List<AccountBaseEntity> accounts) {
        this.accounts = accounts;
    }

    @Override
    public List<AccountBaseEntity> getAccounts() {
        return accounts;
    }

    @Override
    public Collection<TransactionalAccount> toTinkAccounts() {
        return Optional.ofNullable(accounts).orElse(Collections.emptyList()).stream()
                .filter(AccountBaseEntity::isCheckingOrSavingsType)
                .map(AccountBaseEntity::toTinkAccount)
                .collect(Collectors.toList());
    }
}
