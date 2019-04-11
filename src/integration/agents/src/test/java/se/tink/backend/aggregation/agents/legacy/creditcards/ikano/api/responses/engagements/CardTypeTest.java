package se.tink.backend.aggregation.agents.creditcards.ikano.api.responses.engagements;

import org.junit.Assert;
import org.junit.Test;

public class CardTypeTest {
    @Test
    public void cardTypesTest() {
        Assert.assertTrue(CardType.PREEM.hasIdentifier("PREMA"));
        Assert.assertTrue(CardType.PREEM.hasIdentifier("PREEM"));
        Assert.assertFalse(CardType.PREEM.hasIdentifier("SEAT"));
        Assert.assertFalse(CardType.PREEM.hasIdentifier(""));
        Assert.assertFalse(CardType.PREEM.hasIdentifier(null));
    }
}
