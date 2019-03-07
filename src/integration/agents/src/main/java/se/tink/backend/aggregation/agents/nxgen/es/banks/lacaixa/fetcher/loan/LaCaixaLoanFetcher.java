package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.loan;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.LaCaixaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.loan.entities.LoanDetailsAggregate;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.loan.entities.LoanEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.loan.rpc.LoanListResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;

public class LaCaixaLoanFetcher implements AccountFetcher<LoanAccount> {
    private final LaCaixaApiClient apiClient;

    public LaCaixaLoanFetcher(LaCaixaApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<LoanAccount> fetchAccounts() {
        return recurseAllLoans(apiClient.fetchLoansList(true))
                .flatMap(r -> r.getLoans().stream())
                .map(this::fetchLoanDetails)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Optional<LoanAccount> fetchLoanDetails(LoanEntity loan) {
        return Optional.ofNullable(apiClient.fetchLoanDetails(loan.getContractId()))
                .map(loanDetails -> new LoanDetailsAggregate(loan, loanDetails))
                .map(LoanDetailsAggregate::toTinkLoanAccount);
    }

    private Stream<LoanListResponse> recurseAllLoans(LoanListResponse loanListResponse) {
        return loanListResponse.getMoreData()
                ? recurseAllLoans(apiClient.fetchLoansList(false))
                : Stream.of(loanListResponse);
    }
}
