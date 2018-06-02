package se.tink.backend.aggregation.utils.transfer;

import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class StringNormalizerSwedishTest {
    @Test
    public void allowsSwedishCharacters() {
        StringNormalizerSwedish normalizer = new StringNormalizerSwedish();

        String swedishLowercaseCharacters = "abcdefghijklmnopqrstuvwxyzåäö";
        assertThat(normalizer.normalize(swedishLowercaseCharacters))
                .isEqualTo(swedishLowercaseCharacters);

        String swedishUppercaseCharacters = "ABCDEFGHIJKLMNOPQRSTUVWXYZÅÄÖ";
        assertThat(normalizer.normalize(swedishUppercaseCharacters))
                .isEqualTo(swedishUppercaseCharacters);

        String swedishNumbers = "1234567890";
        assertThat(normalizer.normalize(swedishNumbers))
                .isEqualTo(swedishNumbers);
    }

    @Test
    public void replacesLookalikes() {
        StringNormalizerSwedish normalizer = new StringNormalizerSwedish();

        String lookalikes = "é ñ";
        assertThat(normalizer.normalize(lookalikes))
                .isEqualTo("e n");
    }

    @Test
    public void removesSpecialCharacters() {
        StringNormalizerSwedish normalizer = new StringNormalizerSwedish();

        String lookalikes = "# $";
        assertThat(normalizer.normalize(lookalikes))
                .isEqualTo(" ");
    }

    @Test
    public void whiteListAdditionalCharacters() {
        StringNormalizerSwedish normalizer = new StringNormalizerSwedish("#^");

        String lookalikes = "# ^ $";
        assertThat(normalizer.normalize(lookalikes))
                .isEqualTo("# ^ ");
    }

    @Test
    public void getUnchangedCharactersHumanReadable_containsSwedishCharsAndWhiteList() {
        StringNormalizerSwedish normalizer = new StringNormalizerSwedish("#^");

        assertThat(normalizer.getUnchangedCharactersHumanReadable())
                .matches("^a-ö A-Ö 0-9( [#^]){2}$")
                .contains("#")
                .contains("^");
    }
}