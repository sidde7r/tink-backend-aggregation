package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher;

import java.util.Collection;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1ApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.entities.LoanDetailsEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.LoanAccount;

public class Sparebank1LoanFetcher implements AccountFetcher<LoanAccount> {
    private final Sparebank1ApiClient apiClient;

    public Sparebank1LoanFetcher(Sparebank1ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<LoanAccount> fetchAccounts() {
        return apiClient.fetchLoans().getLoans().stream()
                .map(loanEntity -> {
                    LoanDetailsEntity loanDetails = apiClient.fetchLoanDetails(loanEntity.getId());
                    return loanEntity.toTinkLoan(loanDetails);
                })
                .collect(Collectors.toList());
    }
}
