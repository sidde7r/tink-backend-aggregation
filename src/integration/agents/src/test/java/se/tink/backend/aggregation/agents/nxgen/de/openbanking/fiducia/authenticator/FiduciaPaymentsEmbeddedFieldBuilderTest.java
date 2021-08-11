package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.authenticator;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.Test;

public class FiduciaPaymentsEmbeddedFieldBuilderTest {

    @Test
    public void instructionsExtractorShouldWorkAsExpected() {
        // given
        String input =
                "1. Stecken Sie Ihre Chipkarte in den TAN-Generator und drücken \"TAN\"<br>2. Geben Sie den Startcode \"209301603655\" ein und drücken \"OK\"<br>3. Prüfen Sie die Anzeige auf dem Leserdisplay und drücken \"OK\"<br>4. Geben Sie \"die mit 'x' markierten Stellen der Empfänger-IBAN GBxxTCCL0099791827xxxx\" ein und drücken \"OK\"<br>5. Geben Sie \"den Betrag\" ein und drücken \"OK\"<br><br>Bitte geben Sie die auf Ihrem TAN-Generator angezeigte TAN hier ein und bestätigen Sie diese mit \"OK\"";
        // when
        List<String> instructions =
                FiduciaPaymentsEmbeddedFieldBuilder.INSTRUCTION_EXTRACTOR.apply(input);

        // then
        assertThat(instructions)
                .containsExactly(
                        "Stecken Sie Ihre Chipkarte in den TAN-Generator und drücken \"TAN\"",
                        "Geben Sie den Startcode \"209301603655\" ein und drücken \"OK\"",
                        "Prüfen Sie die Anzeige auf dem Leserdisplay und drücken \"OK\"",
                        "Geben Sie \"die mit 'x' markierten Stellen der Empfänger-IBAN GBxxTCCL0099791827xxxx\" ein und drücken \"OK\"",
                        "Geben Sie \"den Betrag\" ein und drücken \"OK\"",
                        "Bitte geben Sie die auf Ihrem TAN-Generator angezeigte TAN hier ein und bestätigen Sie diese mit \"OK\"");
    }
}
