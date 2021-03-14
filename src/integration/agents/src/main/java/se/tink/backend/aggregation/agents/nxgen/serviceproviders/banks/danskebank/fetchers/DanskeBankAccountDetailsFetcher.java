package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.AccountDetailsRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc.AccountDetailsResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@RequiredArgsConstructor
@Slf4j
public class DanskeBankAccountDetailsFetcher {

    private final DanskeBankApiClient apiClient;

    AccountDetailsResponse fetchAccountDetails(String accountNumberInternal) {
        // Use EN language, because information returned in other languages is gibberish.
        try {
            return apiClient.fetchAccountDetails(
                    new AccountDetailsRequest(accountNumberInternal, "EN"));
        } catch (HttpResponseException e) {
            // Sometimes we receive 500 response that has a body of AccountDetailsResponse including
            // interestRate, but missing other fields like accountOwners and accountType - that's
            // why I think it is 500.
            // Try to get the body, so we could set interest rate
            if (e.getResponse().getStatus() == HttpStatus.SC_INTERNAL_SERVER_ERROR
                    && e.getResponse().hasBody()) {
                try {
                    return e.getResponse().getBody(AccountDetailsResponse.class);
                } catch (RuntimeException re) {
                    log.info("Failed to map exception body into AccountDetailsResponse. ", e);
                }
            }
            log.info("Failed to fetch loan account details. ", e);
        }
        return new AccountDetailsResponse();
    }
}
