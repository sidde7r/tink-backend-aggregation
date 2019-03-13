package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount;

import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class BbvaAccountFetcher implements AccountFetcher<TransactionalAccount> {
    private static final AggregationLogger LOGGER = new AggregationLogger(BbvaAccountFetcher.class);
    private final SessionStorage sessionStorage;
    private BbvaApiClient apiClient;

    public BbvaAccountFetcher(BbvaApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        final String holderName = sessionStorage.get(BbvaConstants.StorageKeys.HOLDER_NAME);

        return apiClient
                .fetchProducts()
                .getAccounts()
                .filter(AccountEntity::isTransactionalAccount)
                .filter(AccountEntity::hasBalance)
                .map(account -> account.toTinkAccount(holderName))
                .toJavaList();
    }
}
