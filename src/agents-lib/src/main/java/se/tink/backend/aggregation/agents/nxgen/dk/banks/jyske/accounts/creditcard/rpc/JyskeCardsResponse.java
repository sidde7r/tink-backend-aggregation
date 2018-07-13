package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.accounts.creditcard.rpc;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.accounts.creditcard.entities.JyskeCard;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class JyskeCardsResponse {

    private boolean hasProvisioned;

    private List<JyskeCard> cards;

    public boolean hasCreditCards() {
        return Optional.ofNullable(cards)
                .map(Collection::stream)
                .map(cards -> cards.anyMatch(JyskeCard::isCreditCard))
                .orElse(false);
    }
}
