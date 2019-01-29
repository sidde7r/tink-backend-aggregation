package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30.fetcher.creditcard.rpc;

import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30.fetcher.creditcard.entities.CardEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

@JsonObject
public class FetchCardsResponse {

    private List<CardEntity> cards;

    public List<CreditCardAccount> toTinkCards() {
        return cards.stream()
                .filter(CardEntity::isCreditCard)
                .map(CardEntity::toTinkCard)
                .collect(Collectors.toList());
    }
}
