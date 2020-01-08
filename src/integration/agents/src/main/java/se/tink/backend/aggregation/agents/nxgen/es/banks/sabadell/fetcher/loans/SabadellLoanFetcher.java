package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.loans;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.SabadellApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.SabadellConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.loans.rpc.LoanDetailsRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.loans.rpc.LoanDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.loans.rpc.LoansResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.rpc.ErrorResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class SabadellLoanFetcher implements AccountFetcher<LoanAccount> {
    private final AggregationLogger log = new AggregationLogger(SabadellLoanFetcher.class);
    private final SabadellApiClient apiClient;

    public SabadellLoanFetcher(SabadellApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<LoanAccount> fetchAccounts() {
        try {
            LoansResponse loansResponse = apiClient.fetchLoans();
            if (!loansResponse.getAccounts().isEmpty()) {
                log.infoExtraLong(
                        SerializationUtils.serializeToString(loansResponse),
                        SabadellConstants.Tags.LOANS);

                return loansResponse.getAccounts().stream()
                        .map(account -> apiClient.fetchLoanDetails(new LoanDetailsRequest(account)))
                        .filter(LoanDetailsResponse::hasLoans)
                        .map(LoanDetailsResponse::toTinkLoanAccount)
                        .collect(Collectors.toList());
            }
        } catch (HttpResponseException e) {
            ErrorResponse response = e.getResponse().getBody(ErrorResponse.class);
            String errorCode = response.getErrorCode();

            if (SabadellConstants.ErrorCodes.NO_PRODUCTS.equalsIgnoreCase(errorCode)) {
                return Collections.emptyList();
            }

            log.warn(
                    String.format(
                            "%s: Loan fetching failed with error code: %s, error message: %s",
                            SabadellConstants.Tags.LOAN_ERROR,
                            response.getErrorCode(),
                            response.getErrorMessage()),
                    e);
        }

        return Collections.emptyList();
    }
}
