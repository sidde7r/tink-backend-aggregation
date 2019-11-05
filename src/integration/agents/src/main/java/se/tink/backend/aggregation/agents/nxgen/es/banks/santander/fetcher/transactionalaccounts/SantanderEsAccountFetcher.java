package se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.transactionalaccounts;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.SantanderEsSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.transactionalaccounts.entities.AccountEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class SantanderEsAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final SantanderEsSessionStorage santanderEsSessionStorage;

    public SantanderEsAccountFetcher(final SantanderEsSessionStorage santanderEsSessionStorage) {
        this.santanderEsSessionStorage = santanderEsSessionStorage;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        LoginResponse loginResponse = santanderEsSessionStorage.getLoginResponse();
        return loginResponse.getAccountList().stream()
                .filter(AccountEntity::isKnownAccountType)
                .map(accountEntity -> accountEntity.toTinkAccount(loginResponse))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
