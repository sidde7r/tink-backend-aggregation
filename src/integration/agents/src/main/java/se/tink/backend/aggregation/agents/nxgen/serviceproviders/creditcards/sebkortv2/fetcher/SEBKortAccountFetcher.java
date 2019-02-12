package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkortv2.fetcher;

import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkortv2.SebKortApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkortv2.fetcher.rpc.BillingUnitsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class SEBKortAccountFetcher implements AccountFetcher<CreditCardAccount> {
    private final SebKortApiClient apiClient;

    public SEBKortAccountFetcher(SebKortApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        BillingUnitsResponse billingUnitsResponse = apiClient.fetchBillingUnits();
        return billingUnitsResponse.getCreditCardAccounts();
    }
}
