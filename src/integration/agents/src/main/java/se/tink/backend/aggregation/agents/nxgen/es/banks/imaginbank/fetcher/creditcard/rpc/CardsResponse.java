package se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.creditcard.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.creditcard.entities.CardEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.creditcard.entities.CardListEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

@JsonObject
public class CardsResponse {
    @JsonProperty("listaTarjetas")
    private CardListEntity cardList;

    public CardListEntity getCardList() {
        return cardList;
    }

    @JsonIgnore
    public List<CreditCardAccount> getTinkCreditCards() {
        if (cardList == null || cardList.getCards() == null) {
            return Collections.emptyList();
        }

        return cardList.getCards().stream()
                .filter(CardEntity::isCreditCard)
                .map(CardEntity::toTinkCreditCard)
                .collect(Collectors.toList());
    }
}
