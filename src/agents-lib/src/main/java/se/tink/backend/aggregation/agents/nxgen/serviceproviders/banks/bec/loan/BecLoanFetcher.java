package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.loan;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.accounts.creditcard.BecCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.loan.entities.MortgageLoanEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.rpc.BecErrorResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.LoanAccount;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.agents.rpc.Credentials;

public class BecLoanFetcher implements AccountFetcher<LoanAccount> {
    private static final AggregationLogger log = new AggregationLogger(BecCreditCardFetcher.class);

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
                    .map(loan ->
                            // loanDetails contain the interest rate
                            loan.toTinkLoan(this.apiClient.fetchLoanDetails(loan.getLoanNumber()))
                    )
                    .collect(Collectors.toList());
        } catch (HttpResponseException hre) {
            // -    Requests for users that do not have mortgages will result in a HTTP response with status code 400
            //      and a body with an error message.
            // -    Providers that do not have mortgages in the app return status code 403 (forbidden)

            HttpResponse httpResponse = hre.getResponse();

            switch(httpResponse.getStatus()) {
            case HttpStatus.SC_BAD_REQUEST:
                // Possible that the user did not have mortgage or that details doesn't exist for any loans.
                BecErrorResponse becErrorResponse = this.apiClient.parseBodyAsError(httpResponse);

                if (becErrorResponse.isWithoutMortgage()) {
                    log.info(String.format("%s - User does not have any mortgages", BecConstants.Log.LOANS));
                    return Collections.emptyList();
                }

                if (becErrorResponse.noDetailsExist()) {
                    log.info(String.format("%s - No details for loans exist", BecConstants.Log.LOANS));
                    return Collections.emptyList();
                }

                log.warn(String.format(
                                "%s - Unknown error: [%s] %s",
                                BecConstants.Log.LOAN_FAILED,
                                becErrorResponse.getAction(),
                                becErrorResponse.getMessage()));
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
