package se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CardsListEntity {

    @JsonProperty("tarjetaGenerica")
    private List<GenericCardEntity> cards;

    @JsonProperty("masDatos")
    private boolean moreData;

    public List<GenericCardEntity> getCards() {
        return cards;
    }

    boolean canFetchMore() {
        return moreData;
    }
}
