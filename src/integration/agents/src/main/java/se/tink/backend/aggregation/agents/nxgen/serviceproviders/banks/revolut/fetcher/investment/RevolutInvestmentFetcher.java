package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.fetcher.investment;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.RevolutApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.fetcher.investment.entity.PortfolioResultEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.fetcher.investment.rpc.InvestmentAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.fetcher.investment.rpc.StockInfoResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.fetcher.investment.rpc.StockPriceResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;

public class RevolutInvestmentFetcher implements AccountFetcher<InvestmentAccount> {

    private final RevolutApiClient apiClient;

    public RevolutInvestmentFetcher(RevolutApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
        StockInfoResponse stockInfoResponse = this.apiClient.fetchStockInfo();
        InvestmentAccountResponse accountsResponse = apiClient.fetchInvestmentAccounts();

        List<String> holdingNames = accountsResponse.getHoldingNames();

        if (holdingNames.isEmpty()) {
            return Collections.emptyList();
        }

        StockPriceResponse stockPriceResponse = apiClient.fetchCurrentStockPrice(holdingNames);

        PortfolioResultEntity portfolioResultEntity =
                RevolutInvestmentHelper.calculateResult(
                        stockInfoResponse, stockPriceResponse, accountsResponse);

        return apiClient.fetchInvestmentAccounts().toInvestmentAccounts(portfolioResultEntity);
    }
}
