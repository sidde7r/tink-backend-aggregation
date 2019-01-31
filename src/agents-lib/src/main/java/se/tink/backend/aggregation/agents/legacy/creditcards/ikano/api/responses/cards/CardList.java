package se.tink.backend.aggregation.agents.creditcards.ikano.api.responses.cards;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import java.util.List;
import se.tink.backend.aggregation.agents.creditcards.ikano.api.IkanoApiAgent;
import se.tink.backend.aggregation.agents.creditcards.ikano.api.responses.BaseResponse;
import se.tink.backend.aggregation.agents.creditcards.ikano.api.responses.engagements.CardType;
import se.tink.backend.aggregation.log.AggregationLogger;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CardList extends BaseResponse {
    private static final AggregationLogger log = new AggregationLogger(CardList.class);

    @JsonProperty("Response")
    public List<Card> cards;

    public void logCards() {
        if (cards != null) {
            for (Card card : cards) {
                log.info(MoreObjects.toStringHelper(Card.class)
                        .add("expiresAt", card.getExpiresAt())
                        .add("cardType", card.getCardType())
                        .add("groupCode", card.getGroupCode())
                        .add("hasCredit", card.hasCredit())
                        .add("state", card.getState())
                        .toString());
            }
        } else {
            log.info("No cards found");
        }
    }

    public List<Card> getUnregisteredCards() {
        List<Card> unregisteredCards = Lists.newArrayList();

        for (Card card : cards) {
            if (!card.isRegistered()) {
                unregisteredCards.add(card);
            }
        }

        return unregisteredCards;
    }

    public void keepSelectedCardTypes(CardType cardType) throws IkanoApiAgent.CardNotFoundException {
        ensureHasCards();

        List<Card> cardsWithCorrectType = Lists.newArrayList();

        for (Card card : cards) {
            if (card.isOfType(cardType)) {
                cardsWithCorrectType.add(card);
            }
        }

        cards = cardsWithCorrectType;
        ensureHasCards();
    }

    public void ensureRegisteredCardExists() throws IkanoApiAgent.CardNotFoundException {
        boolean registeredCardExists = false;

        for (Card card : cards) {
            if (card.isRegistered()) {
                registeredCardExists = true;
                break;
            }
        }

        if (!registeredCardExists) {
            throw new IkanoApiAgent.CardNotFoundException();
        }
    }

    private void ensureHasCards() throws IkanoApiAgent.CardNotFoundException {
        if (cards == null || cards.size() == 0) {
            throw new IkanoApiAgent.CardNotFoundException();
        }
    }
}
