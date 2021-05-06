package se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.fetcher.creditcard;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.CajamarApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.entities.CardEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.entities.PositionEntity;
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
                .getPositions()
                .map(PositionEntity::getCards)
                .map(mapToTinkCreditCard())
                .get();
    }

    private Function<List<CardEntity>, List<CreditCardAccount>> mapToTinkCreditCard() {
        return cardEntities ->
                cardEntities.stream()
                        .filter(CardEntity::isCreditCard)
                        .map(
                                cardEntity ->
                                        cardEntity.toTinkCreditCard(
                                                fetchMoreCardInfo(cardEntity.getId())))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList());
    }

    private CreditCardResponse fetchMoreCardInfo(String cardId) {
        return apiClient.fetchCreditCardDetails(cardId);
    }
}
