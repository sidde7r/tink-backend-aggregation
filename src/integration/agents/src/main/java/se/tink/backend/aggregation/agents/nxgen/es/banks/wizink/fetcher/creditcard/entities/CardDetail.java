package se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CardDetail {

    @JsonProperty("cardDetail")
    private CardDetailEntity cardDetailEntity;

    public CardDetailEntity getCardDetailEntity() {
        return cardDetailEntity;
    }
}
