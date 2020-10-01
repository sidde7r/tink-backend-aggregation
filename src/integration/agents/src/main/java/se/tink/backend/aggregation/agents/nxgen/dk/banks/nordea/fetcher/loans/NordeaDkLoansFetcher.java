package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.loans;

import java.util.Collection;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.NordeaDkApiClient;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.loans.entities.LoanEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.loans.rpc.LoanDetailsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;

@RequiredArgsConstructor
public class NordeaDkLoansFetcher implements AccountFetcher<LoanAccount> {

    private final NordeaDkApiClient apiClient;

    @Override
    public Collection<LoanAccount> fetchAccounts() {
        return apiClient.getLoans().getLoans().stream()
                .map(LoanEntity::getLoanId)
                .map(apiClient::getLoanDetails)
                .map(LoanDetailsResponse::toTinkLoanAccount)
                .collect(Collectors.toList());
    }
}
