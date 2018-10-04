package se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.creditcard.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.creditcard.entities.CardListEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CardsResponse {
    @JsonProperty("listaTarjetas")
    private CardListEntity cardList;

    public CardListEntity getCardList() {
        return cardList;
    }
}
