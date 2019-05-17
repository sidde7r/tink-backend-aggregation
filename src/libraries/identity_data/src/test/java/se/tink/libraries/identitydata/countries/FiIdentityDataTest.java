package se.tink.libraries.identitydata.countries;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;
import org.junit.Test;
import se.tink.libraries.identitydata.IdentityData;

public class FiIdentityDataTest {

    @Test
    public void processSsn() {
        assertEquals("311298+9996", FiIdentityData.processSsn("<: 311298+9996"));
        assertEquals("311280-999J", FiIdentityData.processSsn(" 31 12 80 - 999 J\n"));
        assertEquals("131052-308T", FiIdentityData.processSsn("131052-308T"));
        assertEquals("240201A899B", FiIdentityData.processSsn("240201A899B"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidSsnFormat() {
        FiIdentityData.processSsn("240201B899B");
    }

    @Test(expected = IllegalArgumentException.class)
    public void notADateSsn() {
        FiIdentityData.processSsn("351376-1030");
    }

    @Test
    public void getBirthDateFromSsn() {
        assertEquals(LocalDate.of(1898, 12, 31), FiIdentityData.getBirthDateFromSsn("311298+9996"));
        assertEquals(LocalDate.of(1980, 12, 31), FiIdentityData.getBirthDateFromSsn("311280-999J"));
        assertEquals(LocalDate.of(1952, 10, 13), FiIdentityData.getBirthDateFromSsn("131052-308T"));
        assertEquals(LocalDate.of(2001, 2, 24), FiIdentityData.getBirthDateFromSsn("240201A899B"));
    }

    @Test
    public void testBuilder() {
        IdentityData identity = FiIdentityData.of("Juha Pankki", "311298+9996");

        assertEquals(LocalDate.of(1898, 12, 31), identity.getDateOfBirth());
        assertEquals("Juha Pankki", identity.getFullName());
    }
}
