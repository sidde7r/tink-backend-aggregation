package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment;

import com.google.api.client.util.Lists;
import java.util.Collection;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.LansforsakringarApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.LansforsakringarConstants.LogTags;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment.entities.EngagementsEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment.rpc.FetchPensionResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;

public class LansforsakringarInvestmentFetcher implements AccountFetcher<InvestmentAccount> {

    private final LansforsakringarApiClient apiClient;
    private final AggregationLogger log =
            new AggregationLogger(LansforsakringarInvestmentFetcher.class);

    public LansforsakringarInvestmentFetcher(LansforsakringarApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
        final List<InvestmentAccount> investmentAccounts = Lists.newArrayList();
        for (EngagementsEntity engagementsEntity :
                apiClient.fetchPensionWithLifeInsurance().getResponse().getEngagements()) {
            investmentAccounts.add(
                    apiClient
                            .fetchPensionWithLifeInsuranceAgreement(engagementsEntity.getId())
                            .getResponse()
                            .getLifeInsuranceAgreement()
                            .toTinkInvestmentAccount());
        }

        // Log new entities
        FetchPensionResponse pensionResponse = apiClient.fetchPension();
        if (!pensionResponse.getIpsPensionsResponseModel().isEmpty()
                || !pensionResponse.getLivPensionsResponseModel().isPrivatPensionsEmpty()
                || !pensionResponse.getLivPensionsResponseModel().isCapitalInsurancesEmpty()) {
            log.infoExtraLong("Found new unknown entity", LogTags.UNKNOWN_PENSION_TYPE);
        }

        return investmentAccounts;
    }
}
