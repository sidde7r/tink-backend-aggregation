package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.investment;

import java.util.Collection;
import java.util.Collections;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.SparebankenVestApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.investment.rpc.FetchFundInvestmentsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class SparebankenVestInvestmentsFetcher implements AccountFetcher<InvestmentAccount> {

    private final SparebankenVestApiClient apiClient;

    public static SparebankenVestInvestmentsFetcher create(SparebankenVestApiClient apiClient) {
        return new SparebankenVestInvestmentsFetcher(apiClient);
    }

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
        try {
            FetchFundInvestmentsResponse investmentsResponse = apiClient.fetchInvestments();
            return investmentsResponse.getTinkInvestmentAccounts();
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == 403) {
                log.warn("[SparebankenVest] User does not have access to investments.");
                return Collections.emptyList();
            } else {
                throw e;
            }
        }
    }
}
