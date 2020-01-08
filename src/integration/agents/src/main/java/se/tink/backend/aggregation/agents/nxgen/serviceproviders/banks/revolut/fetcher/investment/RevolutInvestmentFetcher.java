package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.fetcher.investment;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.RevolutApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.RevolutConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.fetcher.investment.entity.PortfolioResultEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.fetcher.investment.rpc.InvestmentAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.fetcher.investment.rpc.StockInfoResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.fetcher.investment.rpc.StockPriceResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class RevolutInvestmentFetcher implements AccountFetcher<InvestmentAccount> {

    private final RevolutApiClient apiClient;
    private static final Logger LOGGER = LoggerFactory.getLogger(RevolutInvestmentFetcher.class);

    public RevolutInvestmentFetcher(RevolutApiClient apiClient) {
        this.apiClient = apiClient;
    }

    private Optional<InvestmentAccountResponse> fetchInvestments() {
        try {
            return Optional.of(apiClient.fetchInvestmentAccounts());
        } catch (HttpResponseException hre) {
            LOGGER.info(
                    "{} Unable to fetch investments: [{}]",
                    RevolutConstants.Tags.PORTFOLIO_FETCHING_ERROR,
                    hre.toString());
            return Optional.empty();
        }
    }

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
        StockInfoResponse stockInfoResponse = this.apiClient.fetchStockInfo();

        Optional<InvestmentAccountResponse> accountsResponse = fetchInvestments();

        if (!accountsResponse.isPresent()) {
            return Collections.emptyList();
        }

        List<String> holdingNames = accountsResponse.get().getHoldingNames();

        if (holdingNames.isEmpty()) {
            return Collections.emptyList();
        }

        StockPriceResponse stockPriceResponse = apiClient.fetchCurrentStockPrice(holdingNames);

        PortfolioResultEntity portfolioResultEntity =
                RevolutInvestmentHelper.calculateResult(
                        stockInfoResponse, stockPriceResponse, accountsResponse.get());

        return accountsResponse.get().toInvestmentAccounts(portfolioResultEntity);
    }
}
