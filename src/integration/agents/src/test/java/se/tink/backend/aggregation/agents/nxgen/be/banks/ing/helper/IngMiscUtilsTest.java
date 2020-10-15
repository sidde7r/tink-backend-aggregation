package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.helper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class IngMiscUtilsTest {

    @Test
    public void shouldCleanDescription() {
        String strippedDate =
                IngMiscUtils.cleanDescription("10/10 - 14h05 - DELI BRUUL BVBA - MECHELEN - BEL");
        String strippedVers =
                IngMiscUtils.cleanDescription("Vers: TINKTESTER KBC - BE12345123451234   ");
        String strippedVan =
                IngMiscUtils.cleanDescription("Van: Dhr. Tink Tester - BE12345123451234   ");
        String strippedNaar =
                IngMiscUtils.cleanDescription("Naar: DE H TINK TESTER - BE12345123451234");

        assertThat(strippedDate).isEqualTo("DELI BRUUL BVBA - MECHELEN - BEL");
        assertThat(strippedVers).isEqualTo("TINKTESTER KBC - BE12345123451234");
        assertThat(strippedVan).isEqualTo("Dhr. Tink Tester - BE12345123451234");
        assertThat(strippedNaar).isEqualTo("DE H TINK TESTER - BE12345123451234");
    }
}
