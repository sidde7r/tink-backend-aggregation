package se.tink.backend.aggregation.agents.banks.sbab.model.response;

import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class MortgageSignatureStatusTest {
    @Test
    public void fromStatusCanMapToEnglishEnum() {
        assertThat(MortgageSignatureStatus.fromStatus("NY")).isEqualTo(MortgageSignatureStatus.NEW);
        assertThat(MortgageSignatureStatus.fromStatus("STARTAD")).isEqualTo(MortgageSignatureStatus.STARTED);
        assertThat(MortgageSignatureStatus.fromStatus("LYCKAD")).isEqualTo(MortgageSignatureStatus.SUCCESSFUL);
        assertThat(MortgageSignatureStatus.fromStatus("MISSLYCKAD")).isEqualTo(MortgageSignatureStatus.UNSUCCESSFUL);
        assertThat(MortgageSignatureStatus.fromStatus("AVBRUTEN")).isEqualTo(MortgageSignatureStatus.ABORTED);
        assertThat(MortgageSignatureStatus.fromStatus("FORFALLEN")).isEqualTo(MortgageSignatureStatus.EXPIRED);
    }

    @Test(expected = NullPointerException.class)
    public void fromStatusThrowsOnMissingMapping() {
        MortgageSignatureStatus.fromStatus("NOTMAPPED");
    }
}
