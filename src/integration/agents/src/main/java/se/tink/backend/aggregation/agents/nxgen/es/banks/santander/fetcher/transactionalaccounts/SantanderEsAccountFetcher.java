package se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.transactionalaccounts;

import java.util.Collection;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.SantanderEsSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.transactionalaccounts.entities.AccountEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class SantanderEsAccountFetcher implements AccountFetcher<TransactionalAccount> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SantanderEsAccountFetcher.class);

    private final SantanderEsSessionStorage santanderEsSessionStorage;

    public SantanderEsAccountFetcher(final SantanderEsSessionStorage santanderEsSessionStorage) {
        this.santanderEsSessionStorage = santanderEsSessionStorage;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        // Temporary logging of whole response in String format to detect any unknown accounts
        LOGGER.info("es_santander_full_logging_response", santanderEsSessionStorage.getLoginResponseString());

        LoginResponse loginResponse = santanderEsSessionStorage.getLoginResponse();
        return loginResponse.getAccountList().stream()
                .filter(AccountEntity::isKnownAccountType)
                .map(accountEntity -> accountEntity.toTinkAccount(loginResponse))
                .collect(Collectors.toList());
    }
}
