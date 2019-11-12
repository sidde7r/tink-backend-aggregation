package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.fetcher.creditcard.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.fetcher.creditcard.entities.CardsEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.rpc.SpankkiResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreditCardsResponse extends SpankkiResponse {
    @JsonProperty private List<CardsEntity> cards;

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    public void setCards(List<CardsEntity> cards) {
        this.cards = cards;
    }

    public List<CardsEntity> getCards() {
        return cards;
    }
}
