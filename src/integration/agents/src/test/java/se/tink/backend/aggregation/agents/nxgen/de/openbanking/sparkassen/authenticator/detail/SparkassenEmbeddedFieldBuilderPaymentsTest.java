package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.detail;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.Test;

public class SparkassenEmbeddedFieldBuilderPaymentsTest {

    @Test
    public void instructionsExtractorShouldReturnEmptyListWhenProvidedWithNull() {
        // given
        String input = null;

        // when
        List<String> instructions =
                SparkassenEmbeddedFieldBuilderPayments.INSTRUCTION_EXTRACTOR.apply(input);

        // then
        assertThat(instructions).isEmpty();
    }

    @Test
    public void instructionsExtractorShouldTakeOnlyThingsAfterKeywordAndSplitOnEndOfSentence() {
        // given
        String input =
                "This part will be ignored, up to the moment. The moment I'm looking for is the word Stecken so this is the part of the first line. Second line.Still second line. Third line.";

        // when
        List<String> instructions =
                SparkassenEmbeddedFieldBuilderPayments.INSTRUCTION_EXTRACTOR.apply(input);

        // then
        assertThat(instructions).hasSize(3);
        assertThat(instructions)
                .containsExactly(
                        "Stecken so this is the part of the first line",
                        "Second line.Still second line",
                        "Third line.");
    }
}
