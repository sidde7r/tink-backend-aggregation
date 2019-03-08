package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.loan;

import java.util.Collection;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.NordeaSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.loan.entities.LoansEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.loan.rpc.FetchLoanDetailsResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;

public class NordeaLoanFetcher implements AccountFetcher<LoanAccount> {
    private static final AggregationLogger log = new AggregationLogger(NordeaLoanFetcher.class);

    private final NordeaSEApiClient apiClient;

    public NordeaLoanFetcher(NordeaSEApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<LoanAccount> fetchAccounts() {
        return apiClient.fetchLoans().getLoans().stream()
                .map(LoansEntity::getLoanId)
                .map(apiClient::fetchLoanDetails)
                .map(FetchLoanDetailsResponse::toTinkLoanAccount)
                .collect(Collectors.toList());
    }
}
