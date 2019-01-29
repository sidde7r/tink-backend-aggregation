package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.loans;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.IcaBankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.loans.entities.LoanEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.loans.entities.LoansBodyEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.loans.entities.MortgageEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;

public class IcaBankenLoanFetcher implements AccountFetcher<LoanAccount> {

    private final IcaBankenApiClient apiClient;

    public IcaBankenLoanFetcher(IcaBankenApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<LoanAccount> fetchAccounts() {

        LoansBodyEntity loansBodyEntity = apiClient.fetchLoanOverview();

        Collection<LoanAccount> loanAccounts = loansBodyEntity.getLoanList().getLoans().stream()
                .map(LoanEntity::toTinkLoan)
                .collect(Collectors.toList());

        List<LoanAccount> mortgageAccounts = loansBodyEntity.getMortgageList().getMortgages().stream()
                .map(MortgageEntity::toTinkLoan)
                .collect(Collectors.toList());

        loanAccounts.addAll(mortgageAccounts);

        return loanAccounts;
    }
}
