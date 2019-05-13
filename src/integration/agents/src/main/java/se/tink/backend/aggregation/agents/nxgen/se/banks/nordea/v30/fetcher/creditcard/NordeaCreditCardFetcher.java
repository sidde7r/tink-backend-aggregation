package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.creditcard;

import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.NordeaSEApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class NordeaCreditCardFetcher implements AccountFetcher<CreditCardAccount> {
    private final NordeaSEApiClient apiClient;

    public NordeaCreditCardFetcher(NordeaSEApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        return apiClient.fetchCards().toTinkCards();
    }
}
