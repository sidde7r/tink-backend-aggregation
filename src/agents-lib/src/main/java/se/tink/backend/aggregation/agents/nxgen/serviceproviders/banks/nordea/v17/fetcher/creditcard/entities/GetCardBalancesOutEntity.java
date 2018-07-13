package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.fetcher.creditcard.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GetCardBalancesOutEntity {
    private List<CardsEntity> cards;

    public List<CardsEntity> getCards() {
        return cards;
    }
}
