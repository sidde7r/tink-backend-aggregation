package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.creditcard.rpc;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.creditcard.entities.CardsEntity;

public class FetchCreditCardResponse {
    private List<CardsEntity> cards;

    public List<CardsEntity> getCards() {
        return Optional.ofNullable(cards).orElse(Lists.newArrayList());
    }

    @Override
    public String toString() {
        if (cards == null) {
            return "";
        }
        return cards.stream()
                .filter(CardsEntity::isCredit)
                .map(CardsEntity::toString)
                .reduce("New entity found for credit card:", (c1, c2) -> c1 + "\n" + c2);
    }
}
