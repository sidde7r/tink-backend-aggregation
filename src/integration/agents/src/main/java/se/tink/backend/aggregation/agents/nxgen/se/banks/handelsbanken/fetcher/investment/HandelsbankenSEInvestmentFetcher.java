package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpStatus;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.rpc.SecurityHoldingsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenSessionStorage;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

public class HandelsbankenSEInvestmentFetcher implements AccountFetcher<InvestmentAccount> {
    private final HandelsbankenSEApiClient client;
    private final HandelsbankenSessionStorage sessionStorage;
    private final Credentials credentials;

    private Map<InvestmentAccount, List<Portfolio>> portfoliosByAccount;

    public HandelsbankenSEInvestmentFetcher(
            HandelsbankenSEApiClient client,
            HandelsbankenSessionStorage sessionStorage,
            Credentials credentials) {
        this.client = client;
        this.sessionStorage = sessionStorage;
        this.credentials = credentials;
    }

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
        try {
            return sessionStorage
                    .applicationEntryPoint()
                    .map(client::securitiesHoldings)
                    .map(
                            securityHoldingsResponse ->
                                    securityHoldingsResponse.toTinkInvestments(client, credentials))
                    .orElseGet(Collections::emptyList);
        } catch (HttpResponseException hre) {
            if (handleError(hre)) {
                return Collections.emptyList();
            }

            throw hre;
        }
    }

    private boolean handleError(HttpResponseException hre) {
        if (hre.getResponse().getStatus() == HttpStatus.SC_BAD_REQUEST) {
            HttpResponse response = hre.getResponse();
            if (response != null && response.hasBody()) {
                SecurityHoldingsResponse holdingsResponse =
                        response.getBody(SecurityHoldingsResponse.class);
                if (HandelsbankenSEConstants.Investments.ERROR_TOO_YOUNG_INVESTMENTS
                        .equalsIgnoreCase(holdingsResponse.getCode())) {
                    // If user is too young to use investments in app, just ignore and return empty
                    // list
                    return true;
                }
            }
        }

        return false;
    }
}
