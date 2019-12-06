package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.rpc;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.entities.AccountEntityBaseEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.entities.BerlinGroupAccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@JsonObject
public class AccountsBaseResponseBerlinGroup implements BerlinGroupAccountResponse {
    protected List<AccountEntityBaseEntity> accounts;

    public AccountsBaseResponseBerlinGroup() {}

    public AccountsBaseResponseBerlinGroup(final List<AccountEntityBaseEntity> accounts) {
        this.accounts = accounts;
    }

    public void setAccounts(List<AccountEntityBaseEntity> accounts) {
        this.accounts = accounts;
    }

    @Override
    public List<AccountEntityBaseEntity> getAccounts() {
        return accounts;
    }

    @Override
    public Collection<TransactionalAccount> toTinkAccounts() {
        return Optional.ofNullable(accounts).orElse(Collections.emptyList()).stream()
                .map(BerlinGroupAccountEntity::toTinkAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
