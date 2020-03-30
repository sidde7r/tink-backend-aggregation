package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.fetcher.loan;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.SebApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.fetcher.loan.entities.LoanEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.rpc.Response;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails.Type;

public class SebLoanFetcher implements AccountFetcher<LoanAccount> {

    private final SebApiClient apiClient;

    public SebLoanFetcher(final SebApiClient apiClient) {
        this.apiClient = Objects.requireNonNull(apiClient);
    }

    @Override
    public Collection<LoanAccount> fetchAccounts() {
        final Response response = apiClient.fetchLoans();
        final List<LoanEntity> mortgageLoans = response.getMortgageLoans();
        final List<LoanEntity> blancoLoans = response.getBlancoLoans();

        Stream<LoanAccount> mortgageLoanAccounts =
                mortgageLoans.stream()
                        .map(loanEntity -> loanEntity.toTinkLoanAccount(Type.MORTGAGE));

        Stream<LoanAccount> blancoLoanAccounts =
                blancoLoans.stream().map(loanEntity -> loanEntity.toTinkLoanAccount(Type.BLANCO));

        return Stream.concat(mortgageLoanAccounts, blancoLoanAccounts).collect(Collectors.toList());
    }
}
