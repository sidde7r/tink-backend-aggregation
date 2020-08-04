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
                    BecErrorResponse becErrorResponse =
                            this.apiClient.parseBodyAsError(httpResponse);

                    if (becErrorResponse.isWithoutMortgage()) {
                        logger.info(
                                String.format(
                                        "%s - User does not have any mortgages",
                                        BecConstants.Log.LOANS),
                                hre);
                        return Collections.emptyList();
                    }

                    if (becErrorResponse.noDetailsExist()) {
                        logger.info(
                                String.format(
                                        "%s - No details for loans exist", BecConstants.Log.LOANS),
                                hre);
                        return Collections.emptyList();
                    }

                    // TODO: this is a temporary fix. The endpoint has changed, we have a appstore
                    // monitor card for this and we are working on upgrading it
                    if (becErrorResponse.functionIsNotAvailable()) {
                        logger.info(
                                String.format(
                                        "%s - Function not available", BecConstants.Log.LOANS),
                                hre);
                        return Collections.emptyList();
                    }

                    logger.warn(
                            String.format(
                                    "%s - Unknown error: [%s] %s",
                                    BecConstants.Log.LOAN_FAILED,
                                    becErrorResponse.getAction(),
                                    becErrorResponse.getMessage()),
                            hre);
                    throw hre;
                case HttpStatus.SC_FORBIDDEN:
                    // No mortgages in provider
                    return Collections.emptyList();
                default:
                    throw hre;
            }
        }
    }
}
