package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.creditcard;

import java.util.Collection;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.rpc.FetchProductsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class BbvaCreditCardFetcher implements AccountFetcher<CreditCardAccount> {

    private final BbvaApiClient apiClient;

    public BbvaCreditCardFetcher(BbvaApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        FetchProductsResponse productsResponse = apiClient.fetchProducts();

        return productsResponse
                .getCards()
                .stream()
                .filter(
                        c ->
                                BbvaConstants.AccountTypes.CREDIT_CARD.equals(
                                        c.getSubfamilyTypeCode()))
                .map(c -> c.toTinkCreditCard())
                .collect(Collectors.toList());
    }
}
