package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.loan;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.SBABApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.loan.entities.PropertiesEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.loan.entities.PropertyLoansEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.rpc.AccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;

public class SBABLoanFetcher implements AccountFetcher<LoanAccount> {
    private final SBABApiClient apiClient;

    public SBABLoanFetcher(SBABApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<LoanAccount> fetchAccounts() {
        final AccountsResponse accountsResponse = apiClient.fetchAccounts();

        return accountsResponse.getLoans().getProperties().stream()
                .map(PropertiesEntity::getPropertyLoans)
                .flatMap(List::stream)
                .map(PropertyLoansEntity::toTinkLoanAccount)
                .collect(Collectors.toList());
    }
}
