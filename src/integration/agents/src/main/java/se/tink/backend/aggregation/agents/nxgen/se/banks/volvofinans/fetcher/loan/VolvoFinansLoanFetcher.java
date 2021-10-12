package se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.fetcher.loan;

import java.util.Collection;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.VolvoFinansApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.fetcher.loan.entities.LoanEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;

@RequiredArgsConstructor
public class VolvoFinansLoanFetcher implements AccountFetcher<LoanAccount> {

    private final VolvoFinansApiClient apiClient;

    @Override
    public Collection<LoanAccount> fetchAccounts() {
        return apiClient.fetchLoans().stream()
                .map(LoanEntity::toTinkLoanAccount)
                .collect(Collectors.toList());
    }
}
