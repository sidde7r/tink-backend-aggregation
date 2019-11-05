package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.accounts.creditcard;

import java.util.Collection;
import java.util.Collections;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.JyskeApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class JyskeCreditCardFetcher implements AccountFetcher<CreditCardAccount> {

    private final JyskeApiClient apiClient;

    public JyskeCreditCardFetcher(JyskeApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        return Collections.emptyList();
    }
}
