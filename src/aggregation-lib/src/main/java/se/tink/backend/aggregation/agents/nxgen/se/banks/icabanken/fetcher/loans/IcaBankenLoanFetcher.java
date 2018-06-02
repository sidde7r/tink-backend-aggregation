package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.loans;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.IcaBankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.loans.entities.LoansEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.loans.entities.MortgagesEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.LoanAccount;

public class IcaBankenLoanFetcher implements AccountFetcher<LoanAccount> {

    private final IcaBankenApiClient apiClient;

    public IcaBankenLoanFetcher(IcaBankenApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<LoanAccount> fetchAccounts() {

        List<LoansEntity> loanOverview = apiClient.fetchLoanOverview().getLoans();
        Collection<LoanAccount> loanAccounts = loanOverview.stream().map(LoansEntity::toTinkLoan)
                .collect(Collectors.toList());

        List<MortgagesEntity> mortgageOverview = apiClient.fetchMortgages().getMortgages();
        Collection<LoanAccount> mortgagesAccounts = mortgageOverview.stream().map(MortgagesEntity::toTinkLoan)
                .collect(Collectors.toList());

        loanAccounts.addAll(mortgagesAccounts);

        return loanAccounts;
    }
}
