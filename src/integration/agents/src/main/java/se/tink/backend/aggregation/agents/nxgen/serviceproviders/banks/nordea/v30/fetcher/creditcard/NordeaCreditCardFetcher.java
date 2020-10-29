package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.creditcard;

import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.NordeaBaseApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class NordeaCreditCardFetcher implements AccountFetcher<CreditCardAccount> {
    private final NordeaBaseApiClient apiClient;

    public NordeaCreditCardFetcher(NordeaBaseApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        return apiClient.fetchCards().toTinkCards();
    }
}
