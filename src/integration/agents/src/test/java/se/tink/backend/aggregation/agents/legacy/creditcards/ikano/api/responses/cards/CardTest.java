package se.tink.backend.aggregation.agents.creditcards.ikano.api.responses.cards;

import static org.junit.Assert.*;

import org.junit.Test;
import se.tink.backend.aggregation.agents.creditcards.ikano.api.responses.engagements.CardType;

public class CardTest {
    @Test
    public void ensureCardTypeIsCorrect() {
        Card preemCard = new Card();
        preemCard.setCardType("PREMA");

        Card skodaCard = new Card();
        skodaCard.setCardType("SKODA");

        assertTrue(preemCard.isOfType(CardType.PREEM));
        assertFalse(preemCard.isOfType(CardType.SKODA));
        assertTrue(skodaCard.isOfType(CardType.SKODA));
        assertFalse(skodaCard.isOfType(CardType.PREEM));
    }

    @Test
    public void testIsRegistered() {
        Card card = new Card();

        card.setState("Registered");
        assertTrue(card.isRegistered());

        card.setState("reGiSteRed");
        assertTrue(card.isRegistered());

        card.setState("Unregistered");
        assertFalse(card.isRegistered());
    }
}
