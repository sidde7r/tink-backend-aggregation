package se.tink.backend.aggregation.agents.banks.nordea.utilities;

import org.junit.Assert;
import org.junit.Test;
import se.tink.libraries.account.identifiers.SwedishIdentifier;

public class NordeaAccountIdentifierFormatterTest {

    @Test
    public void testNordeaToSavingsbank() {
        Assert.assertEquals("842280031270465",
                new SwedishIdentifier("8422831270465").getIdentifier(new NordeaAccountIdentifierFormatter()));
    }

    @Test
    public void testNordeaToHandelsbanken() {
        Assert.assertEquals("67690392752158",
                new SwedishIdentifier("6769392752158").getIdentifier(new NordeaAccountIdentifierFormatter()));
    }

    @Test
    public void testNordeaToHandelsbankenPrepended() {
        Assert.assertEquals("67690039275215",
                new SwedishIdentifier("676939275215").getIdentifier(new NordeaAccountIdentifierFormatter()));
    }

    @Test
    public void testNordeaToSEB() {
        Assert.assertEquals("53570077470",
                new SwedishIdentifier("53570077470").getIdentifier(new NordeaAccountIdentifierFormatter()));
    }

    @Test
    public void testNordeaToNordeaSSN() {
        Assert.assertEquals("8607015537",
                new SwedishIdentifier("33008607015537").getIdentifier(new NordeaAccountIdentifierFormatter()));
    }

    @Test
    public void testNordeaToNordea() {
        Assert.assertEquals("16034332648",
                new SwedishIdentifier("16034332648").getIdentifier(new NordeaAccountIdentifierFormatter()));
    }
}
