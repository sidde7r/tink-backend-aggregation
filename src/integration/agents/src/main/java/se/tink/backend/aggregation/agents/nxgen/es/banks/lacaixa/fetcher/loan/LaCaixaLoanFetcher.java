package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.loan;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.LaCaixaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.LaCaixaConstants.LoanTypeName;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.loan.entities.LoanDetailsAggregate;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.loan.entities.LoanEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.loan.rpc.LoanDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.loan.rpc.LoanListResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.rpc.LaCaixaErrorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

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

                if (errorResponse.isUserHasNoLoans() || errorResponse.isNoAccounts()) {
                    return Collections.emptyList();
                }
            }

            throw e;
        }

        return recurseAllLoans(loanListResponse)
                .filter(loanList -> Objects.nonNull(loanList.getLoans()))
                .flatMap(loanList -> loanList.getLoans().stream())
                .map(this::fetchLoanDetailsAggregate)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(LoanDetailsAggregate::toTinkLoanAccount)
                .collect(Collectors.toList());
    }

    private Optional<LoanDetailsAggregate> fetchLoanDetailsAggregate(LoanEntity loanEntity) {
        LoanDetailsResponse loanDetailsResponse;

        switch (loanEntity.getApplicationName().toUpperCase()) {
            case LoanTypeName.MORTGAGE:
                loanDetailsResponse = apiClient.fetchMortgageDetails(loanEntity.getContractId());
                break;
            case LoanTypeName.CONSUMER_LOAN:
                loanDetailsResponse =
                        apiClient.fetchConsumerLoanDetails(loanEntity.getContractId());
                break;
            default:
                return Optional.empty();
        }

        return Optional.of(new LoanDetailsAggregate(loanEntity, loanDetailsResponse));
    }

    private Stream<LoanListResponse> recurseAllLoans(LoanListResponse loanListResponse) {
        return loanListResponse.getMoreData()
                ? recurseAllLoans(apiClient.fetchLoansList(false))
                : Stream.of(loanListResponse);
    }
}
