package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.investment;

import java.util.Collection;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.NordeaFIApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class NordeaInvestmentFetcher implements AccountFetcher<InvestmentAccount> {
    private static Logger LOG = LoggerFactory.getLogger(NordeaInvestmentFetcher.class);
    private final NordeaFIApiClient apiClient;
    private final SessionStorage sessionStorage;

    public NordeaInvestmentFetcher(NordeaFIApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
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

            LOG.debug("User has no agreement for investments");
            return true;
        }

        // user not having confirmed classification for investments could spoil the refresh
        if (errorResponse.hasNoClassification()) {

            LOG.debug("User has not confirmed classification for investments");
            return true;
        }

        // custody account is missing care account
        // Your custody account is missing a care account. It blocks access to the investment
        // section.
        if (errorResponse.hasNoConnectedAccount()) {

            LOG.debug("No account connected to custody account");
            return true;
        }

        return false;
    }
}
