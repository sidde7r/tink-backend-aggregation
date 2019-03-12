package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.investment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.rpc.ProductsResponse;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class BbvaInvestmentFetcher implements AccountFetcher<InvestmentAccount> {
    private static final AggregationLogger LOGGER =
            new AggregationLogger(BbvaInvestmentFetcher.class);
    private final SessionStorage sessionStorage;
    private BbvaApiClient apiClient;

    public BbvaInvestmentFetcher(BbvaApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
        List<InvestmentAccount> accounts = new ArrayList<>();
        ProductsResponse productsResponse = apiClient.fetchProducts();
        String holderName = sessionStorage.get(BbvaConstants.StorageKeys.HOLDER_NAME);

        // investment logging
        logInvestment(
                productsResponse.getInternationalFundsPortfolios(),
                BbvaConstants.LogTags.INVESTMENT_INTERNATIONAL_PORTFOLIO);
        logInvestment(
                productsResponse.getManagedFundsPortfolios(),
                BbvaConstants.LogTags.INVESTMENT_MANAGED_FUNDS);
        logInvestment(
                productsResponse.getWealthDepositaryPortfolios(),
                BbvaConstants.LogTags.INVESTMENT_WEALTH_DEPOSITARY);

        accounts.addAll(
                Optional.ofNullable(productsResponse.getStockAccounts())
                        .orElse(Collections.emptyList()).stream()
                        .map(stockAccount -> stockAccount.toTinkAccount(apiClient, holderName))
                        .collect(Collectors.toList()));

        return accounts;
    }

    private void logInvestment(List<Object> data, LogTag logTag) {
        if (data == null || data.isEmpty()) {
            return;
        }

        try {
            LOGGER.infoExtraLong(SerializationUtils.serializeToString(data), logTag);
        } catch (Exception e) {
            LOGGER.warn(logTag.toString() + " - Failed to log investment data, " + e.getMessage());
        }
    }
}
