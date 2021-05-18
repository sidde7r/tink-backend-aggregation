package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.creditcard;

import java.util.Collection;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.NordeaFIApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

@RequiredArgsConstructor
public class NordeaCreditCardFetcher implements AccountFetcher<CreditCardAccount> {
    private final NordeaFIApiClient apiClient;

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        return apiClient.fetchCards().toTinkCards();
    }
}
