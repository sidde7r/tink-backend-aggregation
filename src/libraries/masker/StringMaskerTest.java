package se.tink.libraries.masker;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class StringMaskerTest {

    @Test
    public void shouldNotMaskNullAndEmptyString() {
        assertThat(StringMasker.starMaskBeginningOfString(null)).isEqualTo(null);
        assertThat(StringMasker.starMaskBeginningOfString("")).isEqualTo("");
    }

    @Test
    public void shouldMaskAllStringIfLengthSmallerThat9() {
        assertThat(StringMasker.starMaskBeginningOfString("1")).isEqualTo("*");
        assertThat(StringMasker.starMaskBeginningOfString("123")).isEqualTo("***");
        assertThat(StringMasker.starMaskBeginningOfString("12345678")).isEqualTo("********");
    }

    @Test
    public void shouldLeave4LastDigitsWhenLengthLongerThan9() {
        assertThat(StringMasker.starMaskBeginningOfString("123456789")).isEqualTo("*****6789");
        assertThat(StringMasker.starMaskBeginningOfString("12345678911131517"))
                .isEqualTo("*************1517");
    }
}
