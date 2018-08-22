package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.HandelsbankenSessionStorage;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.InvestmentAccount;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.system.rpc.Portfolio;

public class HandelsbankenSEInvestmentFetcher implements AccountFetcher<InvestmentAccount> {
    private final HandelsbankenSEApiClient client;
    private final HandelsbankenSessionStorage sessionStorage;
    private final Credentials credentials;

    private Map<InvestmentAccount, List<Portfolio>> portfoliosByAccount;

    public HandelsbankenSEInvestmentFetcher(HandelsbankenSEApiClient client,
            HandelsbankenSessionStorage sessionStorage, Credentials credentials) {
        this.client = client;
        this.sessionStorage = sessionStorage;
        this.credentials = credentials;
    }

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
        return sessionStorage.applicationEntryPoint()
                .map(client::securitiesHoldings)
                .map(securityHoldingsResponse -> securityHoldingsResponse.toTinkInvestments(client, credentials))
                .orElse(Collections.emptyList());
    }
}
