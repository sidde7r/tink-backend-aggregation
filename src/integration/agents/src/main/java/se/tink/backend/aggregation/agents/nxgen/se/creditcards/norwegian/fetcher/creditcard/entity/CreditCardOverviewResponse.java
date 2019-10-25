package se.tink.backend.aggregation.agents.nxgen.se.creditcards.norwegian.fetcher.creditcard.entity;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreditCardOverviewResponse {
    private List<CreditCardEntity> creditCardList;

    public List<CreditCardEntity> getCreditCardList() {
        return creditCardList;
    }
}
