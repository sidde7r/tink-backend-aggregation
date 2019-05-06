package se.tink.libraries.identitydata.countries;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import se.tink.libraries.identitydata.IdentityData;

public class SeIdentityDataTest {

    @Test
    public void processSsn() {
        assertEquals("199202022222", SeIdentityData.processSsn(" 1992-02-02 - 2222 "));
    }

    @Test(expected = IllegalStateException.class)
    public void shortSsn() {
        SeIdentityData.processSsn("560606-2341");
    }

    @Test(expected = IllegalStateException.class)
    public void invalidSsn() {
        SeIdentityData.processSsn("19561232-0000");
    }

    @Test
    public void testConstruction() {

        IdentityData data = SeIdentityData.of("Britt-Marie", "Larsen", "19561231-0021");

        assertEquals("Britt-Marie Larsen", data.getFullName());
        assertEquals("195612310021", data.getSsn());
    }
}
