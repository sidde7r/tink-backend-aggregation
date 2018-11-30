package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30.fetcher.investment;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30.NordeaFiApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30.NordeaFiConstants;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

public class NordeaInvestmentFetcher implements AccountFetcher<InvestmentAccount> {
    private static Logger LOG = LoggerFactory.getLogger(NordeaInvestmentFetcher.class);

    private final NordeaFiApiClient apiClient;

    public NordeaInvestmentFetcher(
            NordeaFiApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
        try {

            return apiClient.fetchInvestments().toTinkInvestmentAccounts();

        } catch (HttpResponseException e) {
            if (isShouldNotFetchInvestmentError(e.getResponse())) {
                return Collections.emptyList();
            }

            throw e;
        }
    }

    private boolean isShouldNotFetchInvestmentError(HttpResponse response) {
        int statusCode = response.getStatus();
        String errorResponse = Optional.ofNullable(response.getBody(String.class)).orElse("").toUpperCase();

        // check status FORBIDDEN, users not having agreement for investments could spoil the refresh
        if (statusCode == HttpStatus.SC_FORBIDDEN &&
                errorResponse.contains(NordeaFiConstants.ErrorCodes.AGREEMENT_NOT_CONFIRMED)) {

            LOG.debug("User has no agreement for investments");
            return true;
        }

        // check status INTERNAL_SERVER_ERROR, custody account is missing care account
        // Your custody account is missing a care account. It blocks access to the investment section.
        if (statusCode == HttpStatus.SC_INTERNAL_SERVER_ERROR &&
                errorResponse.contains(NordeaFiConstants.ErrorCodes.UNABLE_TO_LOAD_CUSTOMER)) {

            LOG.debug("No account connected to custody account");
            return true;
        }

        return false;
    }
}
