package se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.fetcher.transactionalaccount.rpc;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.fetcher.transactionalaccount.entity.account.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.entities.BerlinGroupAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.rpc.BerlinGroupAccountResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@JsonObject
public class FetchAccountsResponse implements BerlinGroupAccountResponse {
    protected List<AccountEntity> accounts;

    public void setAccounts(List<AccountEntity> accounts) {
        this.accounts = accounts;
    }

    @Override
    public List<AccountEntity> getAccounts() {
        return accounts;
    }

    @Override
    public Collection<TransactionalAccount> toTinkAccounts() {
        return Optional.ofNullable(accounts).orElse(Collections.emptyList()).stream()
                .filter(BerlinGroupAccountEntity::isCheckingOrSavingsType)
                .map(BerlinGroupAccountEntity::toTinkAccount)
                .collect(Collectors.toList());
    }
}
