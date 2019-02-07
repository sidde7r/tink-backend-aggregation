package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher;

import java.util.Collection;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1ApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1Constants;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.entities.LoanDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.entities.LoanEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.rpc.LoanListResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;

public class Sparebank1LoanFetcher implements AccountFetcher<LoanAccount> {
    private final Sparebank1ApiClient apiClient;

    public Sparebank1LoanFetcher(Sparebank1ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<LoanAccount> fetchAccounts() {
        return apiClient.getAccounts(Sparebank1Constants.Urls.LOANS, LoanListResponse.class)
                .getLoans().stream()
                .map(this::convertToTinkLoan)
                .collect(Collectors.toList());
    }

    private LoanAccount convertToTinkLoan(LoanEntity loanEntity) {
        LoanDetailsEntity loanDetails = apiClient.fetchLoanDetails(loanEntity.getId());
        return loanEntity.toTinkLoan(loanDetails);
    }
}
