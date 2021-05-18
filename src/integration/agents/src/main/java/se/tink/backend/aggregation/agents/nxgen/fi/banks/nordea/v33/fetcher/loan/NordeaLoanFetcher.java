package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.loan;

import java.util.Collection;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.NordeaFIApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.loan.entities.LoansEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;

@RequiredArgsConstructor
public class NordeaLoanFetcher implements AccountFetcher<LoanAccount> {
    private final NordeaFIApiClient apiClient;

    @Override
    public Collection<LoanAccount> fetchAccounts() {
        return apiClient.fetchLoans().getLoans().stream()
                .map(this::getLoanAccount)
                .collect(Collectors.toList());
    }

    private LoanAccount getLoanAccount(LoansEntity loansEntity) {
        return apiClient.fetchLoanDetails(loansEntity.getLoanId()).toTinkLoanAccount();
    }
}
