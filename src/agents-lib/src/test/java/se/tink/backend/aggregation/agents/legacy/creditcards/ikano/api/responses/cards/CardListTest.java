package se.tink.backend.aggregation.agents.creditcards.ikano.api.responses.cards;

import com.google.common.collect.Lists;
import org.junit.Test;
import se.tink.backend.aggregation.agents.creditcards.ikano.api.IkanoApiAgent;
import se.tink.backend.aggregation.agents.creditcards.ikano.api.responses.engagements.CardType;

import java.util.List;

public class CardListTest {

    @Test(expected = IkanoApiAgent.CardNotFoundException.class)
    public void ensureExceptionIsThrown_whenNoCardsAreFound() throws IkanoApiAgent.CardNotFoundException {
        CardList cards = new CardList();

        cards.keepSelectedCardTypes(CardType.PREEM);
    }

    @Test(expected = IkanoApiAgent.CardNotFoundException.class)
    public void ensureExceptionIsThrown_whenNoCardsMatchesTheSelectedCardType() throws IkanoApiAgent.CardNotFoundException {
        CardList cardList = new CardList();
        List<Card> cards = Lists.newArrayList();

        Card skodaCard = new Card();
        skodaCard.setCardType("SKODA");
        cards.add(skodaCard);

        cardList.cards = cards;

        cardList.keepSelectedCardTypes(CardType.PREEM);
    }

    @Test(expected = IkanoApiAgent.CardNotFoundException.class)
    public void ensureExceptionIsThrown_whenNoRegisteredCardsWereFound() throws IkanoApiAgent.CardNotFoundException {
        CardList cardList = new CardList();
        List<Card> cards = Lists.newArrayList();

        Card card1 = new Card();
        card1.setState("Unregistered");
        Card card2 = new Card();
        card2.setState("Available");

        cards.add(card1);
        cards.add(card2);

        cardList.cards = cards;

        cardList.ensureRegisteredCardExists();
    }

    @Test
    public void ensureExceptionIsNotThrown_whenRegisteredCardsWereFound() throws IkanoApiAgent.CardNotFoundException {
        CardList cardList = new CardList();
        List<Card> cards = Lists.newArrayList();

        Card card1 = new Card();
        card1.setState("Unregistered");
        Card card2 = new Card();
        card2.setState("Registered");

        cards.add(card1);
        cards.add(card2);

        cardList.cards = cards;

        cardList.ensureRegisteredCardExists();
    }
}