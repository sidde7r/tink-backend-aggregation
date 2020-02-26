package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment;

import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.LansforsakringarApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment.rpc.FetchPensionResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment.rpc.FetchPensionWithLifeInsuranceResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;

public class InvestmentFetcher implements AccountFetcher<InvestmentAccount> {

    private LansforsakringarApiClient apiClient;

    public InvestmentFetcher(LansforsakringarApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
//        FetchPensionWithLifeInsuranceResponse fetchPensionWithLifeInsuranceResponse =
//                apiClient.fetchPensionWithLifeInsurance();
        FetchPensionResponse fetchPensionResponse = apiClient.fetchPension();
        // todo: log response if ips pension is present
        FetchPensionWithLifeInsuranceResponse fetchPensionWithLifeInsuranceResponse = apiClient.fetchPensionWithLifeInsurance();
        return null;
    }
}
