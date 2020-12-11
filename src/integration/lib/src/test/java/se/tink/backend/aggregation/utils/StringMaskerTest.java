package se.tink.backend.aggregation.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class StringMaskerTest {

    @Test
    public void shouldNotMaskNullAndEmptyString() {
        assertThat(StringMasker.mask(null)).isEqualTo(null);
        assertThat(StringMasker.mask("")).isEqualTo("");
    }

    @Test
    public void shouldMaskAllStringIfLengthSmallerThat9() {
        assertThat(StringMasker.mask("1")).isEqualTo("*");
        assertThat(StringMasker.mask("123")).isEqualTo("***");
        assertThat(StringMasker.mask("12345678")).isEqualTo("********");
    }

    @Test
    public void shouldLeave4LastDigitsWhenLengthLongerThan9() {
        assertThat(StringMasker.mask("123456789")).isEqualTo("*****6789");
        assertThat(StringMasker.mask("12345678911131517")).isEqualTo("*************1517");
    }
}
