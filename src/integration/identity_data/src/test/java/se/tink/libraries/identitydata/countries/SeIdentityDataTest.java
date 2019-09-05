package se.tink.libraries.identitydata.countries;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;
import org.junit.Test;
import se.tink.libraries.identitydata.IdentityData;

public class SeIdentityDataTest {

    @Test
    public void processSsn() {
        assertEquals("199202022222", SeIdentityData.processSsn(" 1992-02-02 - 2222 "));
        assertEquals("190412310000", SeIdentityData.processSsn("041231+0000"));
    }

    @Test
    public void shortSsn() {
        assertEquals("195606062341", SeIdentityData.processSsn("560606-2341"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidSsn() {
        SeIdentityData.processSsn("19561232-0000");
    }

    @Test
    public void testExtension() {
        assertEquals("199502035555", SeIdentityData.extendSsn("9502035555"));
        assertEquals("191604040000", SeIdentityData.extendSsn("1604040000"));
        assertEquals("200602010000", SeIdentityData.extendSsn("0602010000"));

        LocalDate age_100 = LocalDate.now().minusYears(100);
        LocalDate age_113 = LocalDate.now().minusYears(113);
        String lastFour = "-1111";

        String p1 = (age_100 + lastFour).substring(2);
        String p2 = (age_100.minusDays(1) + lastFour).substring(2);
        String p3 = (age_113.plusDays(1) + lastFour).substring(2);
        String p4 = (age_113 + lastFour).substring(2);
        String p5 = (age_113.minusDays(1) + lastFour).substring(2);

        assertEquals("19", SeIdentityData.processSsn(p1).substring(0, 2)); // 100 years
        assertEquals("19", SeIdentityData.processSsn(p2).substring(0, 2)); // 100 years
        assertEquals("19", SeIdentityData.processSsn(p3).substring(0, 2)); // 112.99 years
        assertEquals("20", SeIdentityData.processSsn(p4).substring(0, 2)); // (1)13 years
        assertEquals("20", SeIdentityData.processSsn(p5).substring(0, 2)); // (1)13 years
    }

    @Test
    public void testConstruction() {
        IdentityData data = SeIdentityData.of("Britt-Marie", "Larsen", "19561231-0021");

        assertEquals("Britt-Marie Larsen", data.getFullName());
        assertEquals("195612310021", data.getSsn());
        assertEquals(LocalDate.of(1956, 12, 31), data.getDateOfBirth());

        IdentityData fnData = SeIdentityData.of("Kalle Kula", "200207314356");

        assertEquals("Kalle Kula", fnData.getFullName());
        assertEquals("200207314356", fnData.getSsn());
        assertEquals(LocalDate.of(2002, 7, 31), fnData.getDateOfBirth());
    }
}
