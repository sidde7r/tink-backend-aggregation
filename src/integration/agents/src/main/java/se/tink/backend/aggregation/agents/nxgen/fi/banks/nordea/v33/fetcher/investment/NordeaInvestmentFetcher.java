package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.investment;

import java.util.Collection;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.NordeaFIApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@Slf4j
@RequiredArgsConstructor
public class NordeaInvestmentFetcher implements AccountFetcher<InvestmentAccount> {
    private final NordeaFIApiClient apiClient;

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

            log.debug("User has no agreement for investments");
            return true;
        }

        // user not having confirmed classification for investments could spoil the refresh
        if (errorResponse.hasNoClassification()) {

            log.debug("User has not confirmed classification for investments");
            return true;
        }

        // custody account is missing care account
        // Your custody account is missing a care account. It blocks access to the investment
        // section.
        if (errorResponse.hasNoConnectedAccount()) {

            log.debug("No account connected to custody account");
            return true;
        }

        return false;
    }
}
