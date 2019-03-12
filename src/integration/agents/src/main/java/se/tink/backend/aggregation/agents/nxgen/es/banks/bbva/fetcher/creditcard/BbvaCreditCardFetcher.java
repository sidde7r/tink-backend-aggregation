package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.creditcard;

import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.creditcard.entities.CreditCardEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class BbvaCreditCardFetcher implements AccountFetcher<CreditCardAccount> {
    private final BbvaApiClient apiClient;

    public BbvaCreditCardFetcher(BbvaApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        return apiClient
                .fetchProducts()
                .getCards()
                .filter(CreditCardEntity::isCreditCard)
                .map(CreditCardEntity::toTinkCreditCard)
                .toJavaList();
    }
}
