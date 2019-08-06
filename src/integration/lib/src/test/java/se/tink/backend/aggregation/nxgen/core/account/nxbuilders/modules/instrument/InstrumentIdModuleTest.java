package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.id.InstrumentIdModule;

public class InstrumentIdModuleTest {
    @Test(expected = NullPointerException.class)
    public void missingIdentifier() {
        InstrumentIdModule.of("SE0378331005", "SE", "name", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void emptyIdentifier() {
        InstrumentIdModule.of("SE0378331005", "SE", "name", "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidIsin() {
        InstrumentIdModule.of("34566", "SE", "name", "12345");
    }

    @Test(expected = NullPointerException.class)
    public void missingName() {
        InstrumentIdModule.of("SE0378331005", "SE", null, "12345");
    }

    @Test(expected = NullPointerException.class)
    public void missingMarketPlace() {
        InstrumentIdModule.of("SE0378331005", null, "name");
    }

    @Test
    public void successfulBuild() {
        InstrumentIdModule idModule = InstrumentIdModule.of("SE0378331005", "SE", "name", "12345");
        assertEquals("SE0378331005", idModule.getIsin());
        assertEquals("SE", idModule.getMarketPlace());
        assertEquals("name", idModule.getName());
        assertEquals("12345", idModule.getUniqueIdentifier());
    }

    @Test
    public void buildWithoutUniqueId() {
        InstrumentIdModule idModule = InstrumentIdModule.of("SE0378331005", "SE", "name");
        assertEquals("SE0378331005", idModule.getIsin());
        assertEquals("SE", idModule.getMarketPlace());
        assertEquals("name", idModule.getName());
        assertEquals("SE0378331005" + "SE", idModule.getUniqueIdentifier());
    }

    @Test
    public void buildWithNameAndUniqueId() {
        InstrumentIdModule idModule = InstrumentIdModule.of(null, null, "name", "12345");
        assertNull(idModule.getIsin());
        assertNull(idModule.getMarketPlace());
        assertEquals("name", idModule.getName());
        assertEquals("12345", idModule.getUniqueIdentifier());
    }

    @Test
    public void builderTest() {
        InstrumentIdModule idModule =
                InstrumentIdModule.builder()
                        .withUniqueIdentifier("12345")
                        .withName("name")
                        .setIsin("SE0378331005")
                        .build();

        assertEquals("SE0378331005", idModule.getIsin());
        assertEquals("name", idModule.getName());
        assertEquals("12345", idModule.getUniqueIdentifier());
        assertNull(idModule.getMarketPlace());
    }

    @Test
    public void builderWithMarketTest() {
        InstrumentIdModule idModule =
                InstrumentIdModule.builder()
                        .withUniqueIdentifier("12345")
                        .withName("name")
                        .setIsin("SE0378331005")
                        .setMarketPlace("SE")
                        .build();

        assertEquals("SE0378331005", idModule.getIsin());
        assertEquals("name", idModule.getName());
        assertEquals("12345", idModule.getUniqueIdentifier());
        assertEquals("SE", idModule.getMarketPlace());
    }
}
