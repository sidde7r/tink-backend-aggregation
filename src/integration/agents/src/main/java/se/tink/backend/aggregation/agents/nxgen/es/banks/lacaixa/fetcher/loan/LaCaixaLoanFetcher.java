package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.loan;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.LaCaixaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.LaCaixaConstants.LoanTypeName;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.LaCaixaConstants.LogTags;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.loan.entities.LoanDetailsAggregate;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.loan.entities.LoanEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.loan.rpc.LoanDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.loan.rpc.LoanListResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.rpc.LaCaixaErrorResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.http.exceptions.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class LaCaixaLoanFetcher implements AccountFetcher<LoanAccount> {
    private static final AggregationLogger LOGGER = new AggregationLogger(LaCaixaLoanFetcher.class);

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
                LOGGER.warnExtraLong(
                        SerializationUtils.serializeToString(loanEntity),
                        LogTags.UNKNOWN_LOAN_CATEGORY);
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
