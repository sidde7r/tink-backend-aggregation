package se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CardListEntity {
    @JsonProperty("tarjeta")
    private List<CardEntity> cards;
    @JsonProperty("masDatos")
    private boolean moreData;

    public List<CardEntity> getCards() {
        return cards;
    }

    public boolean isMoreData() {
        return moreData;
    }
}
