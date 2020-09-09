package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.loans;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.IcaBankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.IcaBankenConstants.Policies;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.loans.entities.LoanEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.loans.entities.LoansBodyEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.loans.entities.MortgageEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.storage.IcaBankenSessionStorage;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;

public class IcaBankenLoanFetcher implements AccountFetcher<LoanAccount> {

    private final IcaBankenApiClient apiClient;
    private final IcaBankenSessionStorage sessionStorage;

    public IcaBankenLoanFetcher(
            IcaBankenApiClient apiClient, IcaBankenSessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public Collection<LoanAccount> fetchAccounts() {
        if (!sessionStorage.hasPolicy(Policies.LOANS)) {
            return Collections.emptyList();
        }

        LoansBodyEntity loansBodyEntity = apiClient.fetchLoanOverview();

        Collection<LoanAccount> loanAccounts =
                loansBodyEntity.getLoanList().getLoans().stream()
                        .map(LoanEntity::toTinkLoan)
                        .collect(Collectors.toList());

        List<LoanAccount> mortgageAccounts =
                loansBodyEntity.getMortgageList().getMortgages().stream()
                        .map(MortgageEntity::toTinkLoan)
                        .collect(Collectors.toList());

        loanAccounts.addAll(mortgageAccounts);

        return loanAccounts;
    }
}
