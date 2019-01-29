package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.loan;

import java.util.Collection;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.SbabApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.loan.entities.LoanEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;

public class SbabLoanFetcher implements AccountFetcher<LoanAccount> {
    private final SbabApiClient apiClient;

    public SbabLoanFetcher(SbabApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<LoanAccount> fetchAccounts() {
        return apiClient
                .listLoans()
                .getLoans()
                .stream()
                .map(LoanEntity::toTinkLoanAccount)
                .collect(Collectors.toList());
    }
}
