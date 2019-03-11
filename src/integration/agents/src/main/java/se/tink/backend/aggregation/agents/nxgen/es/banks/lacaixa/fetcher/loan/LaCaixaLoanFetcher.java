package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.loan;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.LaCaixaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.loan.entities.LoanDetailsAggregate;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.loan.entities.LoanEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.loan.rpc.LoanListResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.rpc.LaCaixaErrorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

public class LaCaixaLoanFetcher implements AccountFetcher<LoanAccount> {
    private final LaCaixaApiClient apiClient;

    public LaCaixaLoanFetcher(LaCaixaApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<LoanAccount> fetchAccounts() {
        LoanListResponse loanListResponse;

        try {
            loanListResponse = apiClient.fetchLoansList(true);
        } catch (HttpResponseException e) {
            HttpResponse response = e.getResponse();

            if (response.getStatus() == HttpStatus.SC_CONFLICT) {
                LaCaixaErrorResponse errorResponse = response.getBody(LaCaixaErrorResponse.class);

                if (errorResponse.isUserHasNoLoans()) {
                    return Collections.emptyList();
                }
            }

            throw e;
        }

        return recurseAllLoans(loanListResponse)
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
