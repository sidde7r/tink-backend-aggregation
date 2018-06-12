package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.investment;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.rpc.FetchProductsResponse;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.InvestmentAccount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class BbvaInvestmentFetcher implements AccountFetcher<InvestmentAccount> {
    private static final AggregationLogger LOGGER = new AggregationLogger(BbvaInvestmentFetcher.class);

    private BbvaApiClient apiClient;

    public BbvaInvestmentFetcher(BbvaApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
        FetchProductsResponse productsResponse = apiClient.fetchProducts();

        // investment logging
        logInvestment(productsResponse.getInternationalFundsPortfolios(),
                BbvaConstants.Logging.INVESTMENT_INTERNATIONAL_PORTFOLIO);
        logInvestment(productsResponse.getManagedFundsPortfolios(),
                BbvaConstants.Logging.INVESTMENT_MANAGED_FUNDS);
        logInvestment(productsResponse.getWealthDepositaryPortfolios(),
                BbvaConstants.Logging.INVESTMENT_WEALTH_DEPOSITARY);

        return Collections.emptyList();
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
