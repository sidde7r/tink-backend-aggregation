package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.investment;

import java.util.Collection;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.NordeaBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.NordeaBaseConstants.LogMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class NordeaInvestmentFetcher implements AccountFetcher<InvestmentAccount> {
    private static Logger LOG = LoggerFactory.getLogger(NordeaInvestmentFetcher.class);

    private final NordeaBaseApiClient apiClient;

    public NordeaInvestmentFetcher(NordeaBaseApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
        try {
            return apiClient.fetchInvestments().toTinkInvestmentAccounts();
        } catch (HttpResponseException e) {
            if (isShouldNotFetchInvestmentError(e)) {
                return Collections.emptyList();
            }

            throw e;
        }
    }

    private boolean isShouldNotFetchInvestmentError(HttpResponseException hre) {
        ErrorResponse errorResponse = ErrorResponse.of(hre);
        // user not having agreement for investments could spoil the refresh
        if (errorResponse.hasNoAgreement()) {
            LOG.debug(LogMessages.NO_INVESTMENTS, hre);
            return true;
        }
        // user not having confirmed classification for investments could spoil the refresh
        if (errorResponse.hasNoClassification()) {
            LOG.debug(LogMessages.NO_CONFIRMED_INVESTMENTS, hre);
            return true;
        }
        // custody account is missing care account
        // Your custody account is missing a care account. It blocks access to the investment
        // section.
        if (errorResponse.hasNoConnectedAccount()) {
            LOG.debug(LogMessages.NO_CUSTODY_ACCOUNT, hre);
            return true;
        }
        return false;
    }
}
