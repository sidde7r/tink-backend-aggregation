package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.loan;

import java.util.Collection;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1ApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1Constants.Urls;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.loan.entities.LoanDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.loan.entities.LoanEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.loan.rpc.LoanListResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;

@Slf4j
public class Sparebank1LoanFetcher implements AccountFetcher<LoanAccount> {
    private final Sparebank1ApiClient apiClient;

    public Sparebank1LoanFetcher(Sparebank1ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<LoanAccount> fetchAccounts() {
        return apiClient.getAccounts(Urls.LOANS, LoanListResponse.class).getLoans().stream()
                .peek(this::logIfNotBasicLoan)
                .map(this::convertToTinkLoan)
                .collect(Collectors.toList());
    }

    private void logIfNotBasicLoan(LoanEntity loanEntity) {
        if (!loanEntity.getType().equals("LOAN")) {
            log.info("Fetched not identified loan type: " + loanEntity.getType());
        }
    }

    private LoanAccount convertToTinkLoan(LoanEntity loanEntity) {
        LoanDetailsEntity loanDetails = apiClient.fetchLoanDetails(loanEntity.getId());
        return loanEntity.toTinkLoan(loanDetails);
    }
}
