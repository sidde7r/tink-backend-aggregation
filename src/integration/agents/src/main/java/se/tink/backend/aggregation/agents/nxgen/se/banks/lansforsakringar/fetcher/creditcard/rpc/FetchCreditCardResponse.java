package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.creditcard.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.creditcard.entities.CardsEntity;

public class FetchCreditCardResponse {
    private List<CardsEntity> cards;

    public List<CardsEntity> getCards() {
        return cards;
    }

    @Override
    public String toString() {
        if (cards == null) {
            return "";
        }
        // We're only interested in non-debit cards
        return cards.stream()
                .filter(CardsEntity::isNotDebit)
                .map(CardsEntity::toString)
                .reduce("New entity found for credit card:", (c1, c2) -> c1 + "\n" + c2);
    }
}
