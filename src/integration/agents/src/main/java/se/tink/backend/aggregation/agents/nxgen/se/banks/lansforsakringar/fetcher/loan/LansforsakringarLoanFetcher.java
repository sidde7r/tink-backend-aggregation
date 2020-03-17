package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.loan;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.LansforsakringarApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.loan.entities.LoansEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.loan.rpc.FetchLoanDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.loan.rpc.FetchLoanOverviewResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class LansforsakringarLoanFetcher implements AccountFetcher<LoanAccount> {

    private final LansforsakringarApiClient apiClient;

    public LansforsakringarLoanFetcher(LansforsakringarApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<LoanAccount> fetchAccounts() {
        final FetchLoanOverviewResponse fetchLoanOverviewResponse;
        try {
            fetchLoanOverviewResponse = apiClient.fetchLoanOverview();
        } catch (HttpResponseException e) {
            return Collections.emptyList();
        }

        return fetchLoanOverviewResponse.getLoans().stream()
                .map(LoansEntity::getLoanNumber)
                .map(apiClient::fetchLoanDetails)
                .map(FetchLoanDetailsResponse::toTinkLoanAccount)
                .collect(Collectors.toList());
    }
}
