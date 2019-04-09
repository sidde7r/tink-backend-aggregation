package se.tink.backend.aggregation.agents.creditcards.ikano.api.responses.cards;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import java.util.List;
import se.tink.backend.aggregation.agents.creditcards.ikano.api.responses.BaseResponse;
import se.tink.backend.aggregation.agents.creditcards.ikano.api.responses.engagements.CardType;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.log.AggregationLogger;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CardList extends BaseResponse {
    private static final AggregationLogger log = new AggregationLogger(CardList.class);

    @JsonProperty("Response")
    public List<Card> cards;

    public void logCards() {
        if (cards != null) {
            for (Card card : cards) {
                log.info(
                        MoreObjects.toStringHelper(Card.class)
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

    public void keepSelectedCardTypes(CardType cardType) throws LoginException {
        ensureHasCards();

        List<Card> cardsWithCorrectType = Lists.newArrayList();

        for (Card card : cards) {
            if (card.isOfType(cardType)) {
                cardsWithCorrectType.add(card);
            }
        }

        cards = cardsWithCorrectType;

        // Throw not a customer exception if user has no cards after filtering. If the user for
        // example selects
        // the shell provider and has an IKEA card the not a customer exception is the response.
        ensureHasCards();
    }

    public void ensureRegisteredCardExists() throws LoginException {
        boolean registeredCardExists = false;

        for (Card card : cards) {
            if (card.isRegistered()) {
                registeredCardExists = true;
                break;
            }
        }

        if (!registeredCardExists) {
            throw LoginError.NOT_CUSTOMER.exception();
        }
    }

    public void ensureHasCards() throws LoginException {
        if (cards == null || cards.size() == 0) {
            throw LoginError.NOT_CUSTOMER.exception();
        }
    }
}
