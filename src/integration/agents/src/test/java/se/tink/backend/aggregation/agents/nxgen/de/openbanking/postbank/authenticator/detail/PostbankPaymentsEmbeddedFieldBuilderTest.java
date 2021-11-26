package se.tink.backend.aggregation.agents.nxgen.de.openbanking.postbank.authenticator.detail;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.Test;

public class PostbankPaymentsEmbeddedFieldBuilderTest {

    @Test
    public void instructions_extractor_should_split_to_seperate_lines() {
        // given
        String input =
                "1. Bitte legen Sie die Debitkarte in Ihren TAN-Generator und drücken Sie die TAN-Taste auf dem TAN-Generator.\n2. Tippen Sie den folgenden Startcode in Ihren TAN-Generator ein und drücken Sie die OK-Taste.\n                Startcode: 123456\n3. Geben Sie die Auftragsdaten in den TAN-Generator ein.\n                Ihre Eingabe:\n                     kontonummer: DE32701694660000123456\n                     betrag: 0,10\n                     \n4. Tragen Sie nun die erzeugte ChipTAN in das Eingabefeld ein und geben Sie den Auftrag frei.\nBitte geben Sie im Feld Konto/IBAN nur die ersten 10 Ziffern der IBAN ein. Beispiel: DE93 5001 AZ78 9012 3456 78 Eingabe: 9350017890";
        // when
        List<String> instructions =
                PostbankPaymentsEmbeddedFieldBuilder.INSTRUCTION_EXTRACTOR.apply(input);

        // then
        assertThat(instructions)
                .containsExactly(
                        "Bitte legen Sie die Debitkarte in Ihren TAN-Generator und drücken Sie die TAN-Taste auf dem TAN-Generator.",
                        "Tippen Sie den folgenden Startcode in Ihren TAN-Generator ein und drücken Sie die OK-Taste.",
                        "Startcode: 123456",
                        "Geben Sie die Auftragsdaten in den TAN-Generator ein.",
                        "Ihre Eingabe:",
                        "kontonummer: DE32701694660000123456",
                        "betrag: 0,10",
                        "",
                        "Tragen Sie nun die erzeugte ChipTAN in das Eingabefeld ein und geben Sie den Auftrag frei.",
                        "Bitte geben Sie im Feld Konto/IBAN nur die ersten 10 Ziffern der IBAN ein. Beispiel: DE93 5001 AZ78 9012 3456 78 Eingabe: 9350017890");
    }
}
