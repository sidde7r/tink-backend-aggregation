package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebopenbanking.fetcher.transactionalaccount.rpc;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebopenbanking.fetcher.transactionalaccount.entities.AccountsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@JsonObject
public class FetchAccountResponse {

    private List<AccountsEntity> accounts;

    public Collection<TransactionalAccount> toTinkAccounts() {
        return Optional.ofNullable(accounts)
                .map(Collection::stream)
                .orElse(Stream.empty())
                .filter(AccountsEntity::isEnabled)
                .map(AccountsEntity::toTinkAccount)
                .collect(Collectors.toList());
    }
}
