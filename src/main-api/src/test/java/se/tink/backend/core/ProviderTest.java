package se.tink.backend.core;

import org.junit.Before;
import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class ProviderTest {

    private static final String SHB_MBI = "Handelsbanken (Mobilt BankID)";
    private static final String SHB_PK = "Handelsbanken (personlig kod)";
    private static final String EUROBONUS_AMERICAN_EXPRESS = "SAS EuroBonus American Express";
    private static final String SEB = "SEB";
    private static final String SWEDBBANK = "Swedbank och Sparbankerna (personlig kod)";

    private static final String CLEAN_SHB_MBI = "Handelsbanken";
    private static final String CLEAN_SHB_PK = "Handelsbanken";
    private static final String CLEAN_EUROBONUS_AMERICAN_EXPRESS = "SAS EuroBonus American Express";
    private static final String CLEAN_SEB = "SEB";
    private static final String CLEAN_SWEDBBANK = "Swedbank och Sparbankerna";

    private Provider shbMbi;
    private Provider shbPk;
    private Provider ae;
    private Provider seb;
    private Provider swedbank;

    @Before
    public void setUp() {
        shbMbi = new Provider();
        shbPk = new Provider();
        ae = new Provider();
        seb = new Provider();
        swedbank = new Provider();

        shbMbi.setDisplayName(SHB_MBI);
        shbPk.setDisplayName(SHB_PK);
        ae.setDisplayName(EUROBONUS_AMERICAN_EXPRESS);
        seb.setDisplayName(SEB);
        swedbank.setDisplayName(SWEDBBANK);
    }

    @Test
    public void testCleanDisplayName() {
        assertEquals(SHB_MBI, shbMbi.getDisplayName());
        assertEquals(SHB_PK, shbPk.getDisplayName());
        assertEquals(EUROBONUS_AMERICAN_EXPRESS, ae.getDisplayName());
        assertEquals(SEB, seb.getDisplayName());
        assertEquals(SWEDBBANK, swedbank.getDisplayName());

        assertEquals(CLEAN_SHB_MBI, shbMbi.getCleanDisplayName());
        assertEquals(CLEAN_SHB_PK, shbPk.getCleanDisplayName());
        assertEquals(CLEAN_EUROBONUS_AMERICAN_EXPRESS, ae.getCleanDisplayName());
        assertEquals(CLEAN_SEB, seb.getCleanDisplayName());
        assertEquals(CLEAN_SWEDBBANK, swedbank.getCleanDisplayName());
    }

    @Test
    public void testProviderCreationWithoutSchedule() {
        Provider provider = new Provider();

        assertThat(provider.getRefreshSchedule().isPresent()).isFalse();
    }

    /**
     * Test will make sure that a schedule can be added to a provider and then read back.
     */
    @Test
    public void testProviderCreationWithSchedule() {
        Provider provider = new Provider();
        provider.setRefreshSchedule(new ProviderRefreshSchedule("10:00", "12:00"));

        assertThat(provider.getRefreshSchedule().isPresent()).isTrue();
        assertThat(provider.getRefreshSchedule().get().getFromString()).isEqualTo("10:00");
        assertThat(provider.getRefreshSchedule().get().getToAsString()).isEqualTo("12:00");
    }
}
