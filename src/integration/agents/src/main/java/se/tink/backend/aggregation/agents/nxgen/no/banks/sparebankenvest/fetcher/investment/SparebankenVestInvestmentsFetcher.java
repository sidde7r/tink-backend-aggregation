package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.investment;

import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.SparebankenVestApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.investment.rpc.FetchFundInvestmentsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.InvestmentAccount;

public class SparebankenVestInvestmentsFetcher implements AccountFetcher<InvestmentAccount> {

    private final SparebankenVestApiClient apiClient;

    private SparebankenVestInvestmentsFetcher(SparebankenVestApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public static SparebankenVestInvestmentsFetcher create(SparebankenVestApiClient apiClient) {
        return new SparebankenVestInvestmentsFetcher(apiClient);
    }

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
        FetchFundInvestmentsResponse investmentsResponse = this.apiClient.fetchInvestments();
        return investmentsResponse.getTinkInvestmentAccounts();
    }
}
