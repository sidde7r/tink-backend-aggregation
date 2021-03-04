package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.loan;

import com.google.common.base.Strings;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.NordeaBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.loan.entities.LoansEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.loan.rpc.LoanDetailsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;

public class NordeaLoanFetcher implements AccountFetcher<LoanAccount> {
    private final NordeaBaseApiClient apiClient;

    public NordeaLoanFetcher(NordeaBaseApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<LoanAccount> fetchAccounts() {
        return apiClient.fetchLoans().getLoans().stream()
                .map(this::parseLoanAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Optional<LoanAccount> parseLoanAccount(LoansEntity loansEntity) {

        if (Strings.isNullOrEmpty(loansEntity.getProductCode())) {
            return loansEntity.toBasicTinkLoanAccount();
        }

        LoanDetailsResponse loanDetails = apiClient.fetchLoanDetails(loansEntity.getLoanId());
        return loanDetails.toTinkLoanAccount();
    }
}
