package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.investment;

import java.util.Collection;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.NordeaSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.NordeaSEConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

public class NordeaInvestmentFetcher implements AccountFetcher<InvestmentAccount> {
    private static Logger LOG = LoggerFactory.getLogger(NordeaInvestmentFetcher.class);

    private final NordeaSEApiClient apiClient;

    public NordeaInvestmentFetcher(NordeaSEApiClient apiClient) {
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
        ErrorResponse errorResponse = response.getBody(ErrorResponse.class);
        // user not having agreement for investments could spoil the refresh
        if (errorResponse.hasNoAgreement()) {
            LOG.debug(NordeaSEConstants.LogMessages.NO_INVESTMENTS);
            return true;
        }
        // user not having confirmed classification for investments could spoil the refresh
        if (errorResponse.hasNoClassification()) {
            LOG.debug(NordeaSEConstants.LogMessages.NO_CONFIRMED_INVESTMENTS);
            return true;
        }
        // custody account is missing care account
        // Your custody account is missing a care account. It blocks access to the investment
        // section.
        if (errorResponse.hasNoConnectedAccount()) {
            LOG.debug(NordeaSEConstants.LogMessages.NO_CUSTODY_ACCOUNT);
            return true;
        }
        return false;
    }
}
