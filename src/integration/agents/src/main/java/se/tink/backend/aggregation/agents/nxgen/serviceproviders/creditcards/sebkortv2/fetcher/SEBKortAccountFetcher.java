package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkortv2.fetcher;

import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkortv2.SebKortApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkortv2.SebKortConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkortv2.fetcher.rpc.BillingUnitsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class SEBKortAccountFetcher implements AccountFetcher<CreditCardAccount> {
    private final SebKortApiClient apiClient;
    private final SebKortConfiguration config;
    private final String currency;

    public SEBKortAccountFetcher(
            SebKortApiClient apiClient, SebKortConfiguration config, String currency) {
        this.apiClient = apiClient;
        this.config = config;
        this.currency = currency;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        BillingUnitsResponse billingUnitsResponse = apiClient.fetchBillingUnits();

        return billingUnitsResponse.getCreditCardAccounts(config, currency);
    }
}
