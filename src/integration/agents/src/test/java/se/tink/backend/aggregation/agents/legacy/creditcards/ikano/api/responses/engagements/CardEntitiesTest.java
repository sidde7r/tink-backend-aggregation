package se.tink.backend.aggregation.agents.creditcards.ikano.api.responses.engagements;

import com.google.common.collect.Lists;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.LoginException;

public class CardEntitiesTest {

    @Test
    public void testValidCardResponse() throws LoginException {
        List<CardEntity> cards = createCards();

        CardEntities response = new CardEntities();
        response.setCards(cards);
        response.keepSelectedCardTypes(CardType.PREEM);
        cards = response.getCards();

        Assertions.assertThat(cards.size()).isEqualTo(3);

        for (CardEntity card : cards) {
            Assertions.assertThat(card.isOfType(CardType.PREEM)).isTrue();
        }
    }

    @Test(expected = LoginException.class)
    public void throwsWhenNoCardsWereFound() throws LoginException {
        CardEntities response = new CardEntities();
        response.keepSelectedCardTypes(CardType.PREEM);
    }

    @Test(expected = LoginException.class)
    public void throwsIfSelectedCardTypeDoesNotExist() throws LoginException {
        List<CardEntity> cards = createCards();

        CardEntities response = new CardEntities();
        response.setCards(cards);
        response.keepSelectedCardTypes(CardType.SKODA);
    }

    private List<CardEntity> createCards() {
        List<CardEntity> cards = Lists.newArrayList();

        cards.add(createCard("PREMA"));
        cards.add(createCard("PREEM"));
        cards.add(createCard("SHEMA"));
        cards.add(createCard("PREMA"));
        cards.add(createCard("OTHER"));

        return cards;
    }

    private CardEntity createCard(String productCode) {
        CardEntity card = new CardEntity();
        card.setCardType(productCode);

        return card;
    }
}
