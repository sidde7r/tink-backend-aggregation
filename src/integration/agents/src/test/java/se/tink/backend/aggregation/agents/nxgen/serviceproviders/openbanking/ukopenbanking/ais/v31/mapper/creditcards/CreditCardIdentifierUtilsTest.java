package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.creditcards;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class CreditCardIdentifierUtilsTest {

    @Test
    public void shouldReturnZeroWhenCardIdentifierIsNull() {
        // when
        int actualLength = CreditCardIdentifierUtils.getCardIdentifierLength(null);

        // then
        Assertions.assertThat(actualLength).isZero();
    }

    @Test
    public void shouldReturnNonZeroWhenCardIdentifierIsNotNull() {
        // given
        String cardIdentifier = "5555-55XX-XXXX-1111";

        // when
        int actualLength = CreditCardIdentifierUtils.getCardIdentifierLength(cardIdentifier);

        // then
        Assertions.assertThat(actualLength).isEqualTo(16);
    }

    @Test
    public void shouldCountOnlyCharsWithoutWhitespaces() {
        // given
        String cardIdentifier = "5555 55XX XXXX 1111";

        // when
        int actualLength = CreditCardIdentifierUtils.getCardIdentifierLength(cardIdentifier);

        // then
        Assertions.assertThat(actualLength).isEqualTo(16);
    }

    @Test
    public void shouldReturnUnknownWhenIdentifierIsNull() {
        // when
        String actualResult = CreditCardIdentifierUtils.isMaskedIdentifier(null);

        // then
        Assertions.assertThat(actualResult).isEqualToIgnoringCase("empty");
    }

    @Test
    public void shouldReturnUnknownWhenIdentifierIsEmptyString() {
        // when
        String actualResult = CreditCardIdentifierUtils.isMaskedIdentifier("");

        // then
        Assertions.assertThat(actualResult).isEqualToIgnoringCase("empty");
    }

    @Test
    public void shouldReturnTrueWhenIdentifierIsContainingAsterisk() {
        // given
        String cardIdentifier = "5555-55**-****-1111";

        // when
        String actualResult = CreditCardIdentifierUtils.isMaskedIdentifier(cardIdentifier);

        // then
        Assertions.assertThat(actualResult).isEqualToIgnoringCase("true");
    }

    @Test
    public void shouldReturnTrueWhenIdentifierIsContainingXChar() {
        // given
        String cardIdentifier = "5555-55XX-XXXX-1111";

        // when
        String actualResult = CreditCardIdentifierUtils.isMaskedIdentifier(cardIdentifier);

        // then
        Assertions.assertThat(actualResult).isEqualToIgnoringCase("true");
    }

    @Test
    public void shouldReturnTrueWhenIdentifierIsContainingXCharWithWhitespaces() {
        // given
        String cardIdentifier = "5555 55XX XXXX 1111";

        // when
        String actualResult = CreditCardIdentifierUtils.isMaskedIdentifier(cardIdentifier);

        // then
        Assertions.assertThat(actualResult).isEqualToIgnoringCase("true");
    }

    @Test
    public void shouldReturnFalseWhenIdentifierIsContainingOnlyDigits() {
        // given
        String cardIdentifier = "5555-5500-0000-1111";

        // when
        String actualResult = CreditCardIdentifierUtils.isMaskedIdentifier(cardIdentifier);

        // then
        Assertions.assertThat(actualResult).isEqualToIgnoringCase("false");
    }

    @Test
    public void shouldReturnFalseWhenIdentifierWithWhitespacesIsContainingOnlyDigits() {
        // given
        String cardIdentifier = "5555 5500 0000 1111";

        // when
        String actualResult = CreditCardIdentifierUtils.isMaskedIdentifier(cardIdentifier);

        // then
        Assertions.assertThat(actualResult).isEqualToIgnoringCase("false");
    }
}
