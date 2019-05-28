package se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander.fetcher.transactionalaccount.rpc;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.entities.AccountBaseEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.rpc.AccountsBaseResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@JsonObject
public class AccountsResponse extends AccountsBaseResponse {

    private List<AccountEntity> accounts;

    @Override
    public Collection<TransactionalAccount> toTinkAccounts() {
        return Optional.ofNullable(accounts).orElse(Collections.emptyList()).stream()
                .filter(AccountBaseEntity::isCheckingOrSavingsType)
                .map(AccountEntity::toTinkAccount)
                .collect(Collectors.toList());
    }
}
