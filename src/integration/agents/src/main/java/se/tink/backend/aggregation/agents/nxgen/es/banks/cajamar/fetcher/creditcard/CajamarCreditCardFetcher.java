package se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.fetcher.creditcard;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.CajamarApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.fetcher.creditcard.rpc.CreditCardResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class CajamarCreditCardFetcher implements AccountFetcher<CreditCardAccount> {

    private final CajamarApiClient apiClient;

    public CajamarCreditCardFetcher(CajamarApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        return apiClient
                .fetchPositions()
                .getCards()
                .map(
                        cardEntity ->
                                cardEntity.toTinkCreditCard(fetchMoreCardInfo(cardEntity.getId())))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private CreditCardResponse fetchMoreCardInfo(String cardId) {
        return apiClient.fetchCreditCardDetails(cardId);
    }
}
