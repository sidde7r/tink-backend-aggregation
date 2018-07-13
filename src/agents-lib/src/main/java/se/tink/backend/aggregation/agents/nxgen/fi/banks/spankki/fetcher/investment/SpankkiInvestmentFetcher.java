package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.fetcher.investment;

import java.util.Collection;
import java.util.Map;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.SpankkiApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.fetcher.investment.rpc.FundsPortfoliosResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.InvestmentAccount;

public class SpankkiInvestmentFetcher implements AccountFetcher<InvestmentAccount> {

    private final SpankkiApiClient apiClient;

    public SpankkiInvestmentFetcher(SpankkiApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
        FundsPortfoliosResponse fundsPortfoliosResponse = this.apiClient.fetchFundsPortfolios();
        Map<String, String> fundIdIsinMapper = this.apiClient.fetchAllFunds().getFundIdIsinMapper();
        return fundsPortfoliosResponse.toTinkInvestmentAccounts(fundIdIsinMapper);
    }
}
