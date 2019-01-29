package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.rpc.FetchProductsResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class BbvaAccountFetcher implements AccountFetcher<TransactionalAccount> {
    private static final AggregationLogger LOGGER = new AggregationLogger(BbvaAccountFetcher.class);

    private BbvaApiClient apiClient;
    private final SessionStorage sessionStorage;

    public BbvaAccountFetcher(BbvaApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        HolderName holderName = Optional.ofNullable(sessionStorage.get(BbvaConstants.Storage.HOLDER_NAME))
                .map(holdername -> new HolderName(holdername))
                .orElse(null);

        FetchProductsResponse productsResponse = apiClient.fetchProducts();

        if (productsResponse == null || productsResponse.getAccounts() == null) {
            return Collections.emptyList();
        }

        logUnknownAccountTypes(productsResponse.getAccounts());

        return productsResponse.getAccounts().stream()
                .filter(AccountEntity::isKnownAccountType)
                .filter(AccountEntity::hasBalance)
                .map(account -> account.toTinkAccount(holderName))
                .collect(Collectors.toList());
    }

    private void logUnknownAccountTypes(List<AccountEntity> accounts) {
        try {
            accounts.stream()
                    .filter(a -> !a.isKnownAccountType())
                    .forEach(a -> {
                        LOGGER.infoExtraLong(SerializationUtils.serializeToString(a), BbvaConstants.Logging.UNKNOWN_ACCOUNT_TYPE);
                    });
        } catch (Exception e) {
            LOGGER.warn("Failed to log unknown account type, " + e.getMessage());
        }
    }
}
