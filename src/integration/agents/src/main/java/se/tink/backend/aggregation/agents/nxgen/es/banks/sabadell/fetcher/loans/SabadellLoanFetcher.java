package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.loans;

import com.google.common.base.Strings;
import java.util.Collection;
import java.util.Collections;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.SabadellApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.SabadellConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.rpc.ErrorResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

public class SabadellLoanFetcher implements AccountFetcher<LoanAccount> {
    private final AggregationLogger log = new AggregationLogger(SabadellLoanFetcher.class);
    private final SabadellApiClient apiClient;

    public SabadellLoanFetcher(SabadellApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<LoanAccount> fetchAccounts() {
        try {
            String loansResponseString = apiClient.fetchLoans();
            if (!Strings.isNullOrEmpty(loansResponseString)) {
                log.infoExtraLong(loansResponseString, SabadellConstants.Tags.LOANS);
            }
        } catch (HttpResponseException e) {
            ErrorResponse response = e.getResponse().getBody(ErrorResponse.class);
            String errorCode = response.getErrorCode();

            if (SabadellConstants.ErrorCodes.NO_PRODUCTS.equalsIgnoreCase(errorCode)) {
                return Collections.emptyList();
            }

            log.warn(String.format(
                    "%s: Loan fetching failed with error code: %s, error message: %s",
                    SabadellConstants.Tags.LOAN_ERROR,
                    response.getErrorCode(),
                    response.getErrorMessage()));
        }

        return Collections.emptyList();
    }
}
