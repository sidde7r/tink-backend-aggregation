package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment;

import java.util.Collection;
import java.util.Collections;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.LansforsakringarApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment.entities.EngagementsEntity;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;

public class InvestmentFetcher implements AccountFetcher<InvestmentAccount> {

    private final LansforsakringarApiClient apiClient;
    private final AggregationLogger log = new AggregationLogger(InvestmentFetcher.class);

    public InvestmentFetcher(LansforsakringarApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
        //        FetchPensionResponse fetchPensionResponse = apiClient.fetchPension();
        Collection<InvestmentAccount> investmentAccounts = Collections.emptyList();
        for (EngagementsEntity engagementsEntity :
                apiClient.fetchPensionWithLifeInsurance().getResponse().getEngagements()) {
            investmentAccounts.add(
                    apiClient
                            .fetchPensionWithLifeInsuranceAgreement(engagementsEntity.getId())
                            .getResponse()
                            .getLifeInsuranceAgreement()
                            .toTinkInvestmentAccount());
        }

        // todo: log response if ips pension is present
        //        FetchPensionResponse pensionResponse = apiClient.fetchPension();
        //        if (pensionResponse.getIpsPensionsResponseModel().)

        return investmentAccounts;
    }
}
