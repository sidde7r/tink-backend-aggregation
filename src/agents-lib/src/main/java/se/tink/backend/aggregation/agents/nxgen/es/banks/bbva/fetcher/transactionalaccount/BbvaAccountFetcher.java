package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.rpc.AccountBalanceResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.rpc.FetchProductsResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.core.Amount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class BbvaAccountFetcher implements AccountFetcher<TransactionalAccount> {
    private static final AggregationLogger LOGGER = new AggregationLogger(BbvaAccountFetcher.class);

    private BbvaApiClient apiClient;

    public BbvaAccountFetcher(BbvaApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        FetchProductsResponse productsResponse = apiClient.fetchProducts();

        if (productsResponse == null || productsResponse.getAccounts() == null) {
            return Collections.emptyList();
        }

        logUnknownAccountTypes(productsResponse.getAccounts());

        return productsResponse.getAccounts().stream()
                .filter(AccountEntity::isKnownAccountType)
                .filter(accountEntity -> {
                    // Filter out any account that does not have a balance (for now).
                    if (!accountEntity.hasBalance()) {
                        // Temporary log code.
                        logNullBalance(accountEntity.getId());
                        return false;
                    }
                    return true;
                })
                .map(AccountEntity::toTinkAccount)
                .collect(Collectors.toList());
    }

    // Experiment to see if we can get the balance through another endpoint for accounts that have null balance.
    private void logNullBalance(String accountId) {
        try {
            AccountBalanceResponse accountBalanceResponse = apiClient.fetchAccountBalance(accountId);
            Optional<Amount> tinkAmount = accountBalanceResponse.getTinkAmountForId(accountId);

            LOGGER.info(
                    String.format("%s: accountId: %s, hasAmount: %s, amount: %s",
                            BbvaConstants.Logging.NULL_ACCOUNT_BALANCE,
                            accountId,
                            tinkAmount.isPresent(),
                            tinkAmount.orElse(new Amount(null, null)).toString())
            );
        } catch (Exception e) {
            // nop
            LOGGER.warn(BbvaConstants.Logging.NULL_ACCOUNT_BALANCE.toString(), e);
        }
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
