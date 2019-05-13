package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.creditcard.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.creditcard.entities.CardsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

@JsonObject
public class FetchCardsResponse {
    @JsonProperty private List<CardsEntity> cards;

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    public void setCards(List<CardsEntity> cards) {
        this.cards = cards;
    }

    public List<CardsEntity> getCards() {
        return cards;
    }

    @JsonIgnore
    public List<CreditCardAccount> toTinkCards() {
        return getCards().stream()
                .filter(CardsEntity::isCreditCard)
                .map(CardsEntity::toTinkCard)
                .collect(Collectors.toList());
    }
}
