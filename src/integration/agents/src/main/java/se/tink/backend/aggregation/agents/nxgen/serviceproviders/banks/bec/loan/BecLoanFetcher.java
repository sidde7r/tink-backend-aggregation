package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.loan;

import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.loan.entities.MortgageLoanEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.rpc.BecErrorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class BecLoanFetcher implements AccountFetcher<LoanAccount> {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final BecApiClient apiClient;
    private final Credentials credentials;

    private static final String UNKNOWN_ERROR_TEMPLATE =
            BecConstants.Log.LOAN_FAILED + " - Unknown error: [{}] {} HTTP_CODE: {}";

    public BecLoanFetcher(BecApiClient apiClient, Credentials credentials) {
        this.apiClient = apiClient;
        this.credentials = credentials;
    }

    @Override
    public Collection<LoanAccount> fetchAccounts() {
        try {
            List<MortgageLoanEntity> loans = this.apiClient.fetchLoans();
            return loans.stream()
                    .map(
                            loan ->
                                    // loanDetails contain the interest rate
                                    loan.toTinkLoan(
                                            this.apiClient.fetchLoanDetails(loan.getLoanNumber())))
                    .collect(Collectors.toList());
        } catch (HttpResponseException hre) {
            // -    Requests for users that do not have mortgages will result in a HTTP response
            // with status code 400
            //      and a body with an error message.
            // -    Providers that do not have mortgages in the app return status code 403
            // (forbidden)

            HttpResponse httpResponse = hre.getResponse();

            switch (httpResponse.getStatus()) {
                case HttpStatus.SC_BAD_REQUEST:
                    // Possible that the user did not have mortgage or that details doesn't exist
                    // for any loans.
                    BecErrorResponse becErrorResponse = apiClient.parseBodyAsError(httpResponse);

                    if (becErrorResponse.isKnownMessage()) {
                        logger.info(
                                "{} - message: {} reason: {}",
                                BecConstants.Log.LOANS,
                                becErrorResponse.getMessage(),
                                becErrorResponse.getReason());
                        return Collections.emptyList();
                    } else {
                        logger.warn(
                                UNKNOWN_ERROR_TEMPLATE,
                                becErrorResponse.getAction(),
                                becErrorResponse.getMessage(),
                                httpResponse.getStatus(),
                                hre);
                        throw hre;
                    }
                case HttpStatus.SC_FORBIDDEN:
                    logger.warn(
                            UNKNOWN_ERROR_TEMPLATE,
                            "",
                            hre.getResponse().getBody(String.class),
                            httpResponse.getStatus(),
                            hre);
                    return Collections.emptyList();
                default:
                    logger.warn(
                            UNKNOWN_ERROR_TEMPLATE,
                            "",
                            hre.getResponse().getBody(String.class),
                            httpResponse.getStatus(),
                            hre);
                    throw hre;
            }
        }
    }
}
