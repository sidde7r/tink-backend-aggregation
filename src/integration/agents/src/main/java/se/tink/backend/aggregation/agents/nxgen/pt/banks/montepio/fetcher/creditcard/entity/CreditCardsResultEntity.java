package se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.fetcher.creditcard.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreditCardsResultEntity {
    @JsonProperty("CustomerProducts")
    private List<CreditCardEntity> creditCards;

    public List<CreditCardEntity> getCreditCards() {
        return creditCards != null ? creditCards : Collections.emptyList();
    }
}
