package se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.fetcher.loans;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.MontepioApiClient;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.entities.AccountDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.rpc.FetchAccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;

public class MontepioLoansFetcher implements AccountFetcher<LoanAccount> {

    private final MontepioApiClient apiClient;

    public MontepioLoansFetcher(final MontepioApiClient apiClient) {
        this.apiClient = Objects.requireNonNull(apiClient);
    }

    @Override
    public Collection<LoanAccount> fetchAccounts() {
        FetchAccountsResponse response = apiClient.fetchLoans();
        return response.getResult().getAccounts().orElseGet(Collections::emptyList).stream()
                .map(this::mapLoan)
                .collect(Collectors.toList());
    }

    private LoanAccount mapLoan(AccountEntity entity) {
        return entity.toLoanAccount(
                apiClient.fetchLoanDetails(entity.getHandle()).getResult().getAccountDetails()
                        .stream()
                        .collect(
                                Collectors.toMap(
                                        AccountDetailsEntity::getKey,
                                        AccountDetailsEntity::getValue)));
    }
}
