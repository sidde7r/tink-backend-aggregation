package se.tink.libraries.masker;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class StringMaskerTest {

    @Test
    public void shouldNotStarMaskIfNullAndEmptyString() {
        assertThat(StringMasker.starMaskBeginningOfString(null)).isEqualTo(null);
        assertThat(StringMasker.starMaskBeginningOfString("")).isEqualTo("");
    }

    @Test
    public void shouldStarMaskAllCharactersIfLengthUpTo8() {
        assertThat(StringMasker.starMaskBeginningOfString("1")).isEqualTo("*");
        assertThat(StringMasker.starMaskBeginningOfString("123")).isEqualTo("***");
        assertThat(StringMasker.starMaskBeginningOfString("12345678")).isEqualTo("********");
    }

    @Test
    public void shouldStarMaskStringExceptLast4CharactersIfLongerThan8() {
        assertThat(StringMasker.starMaskBeginningOfString("123456789")).isEqualTo("*****6789");
        assertThat(StringMasker.starMaskBeginningOfString("12345678911131517"))
                .isEqualTo("*************1517");
    }

    @Test
    public void shouldNotMaskMiddleOfStringIfNullAndEmpty() {
        assertThat(StringMasker.maskMiddleOfString(null)).isEqualTo(null);
        assertThat(StringMasker.maskMiddleOfString("")).isEqualTo("");
    }

    @Test
    public void shouldMaskAllCharactersInsteadOfJustMiddleIfLengthUpTo16() {
        assertThat(StringMasker.maskMiddleOfString("1")).isEqualTo("~***~");
        assertThat(StringMasker.maskMiddleOfString("123")).isEqualTo("~***~");
        assertThat(StringMasker.maskMiddleOfString("12345678")).isEqualTo("~***~");
    }

    @Test
    public void shouldMaskMiddleOfStringIfLongerThan16() {
        assertThat(StringMasker.maskMiddleOfString("12345678901234567")).isEqualTo("1234~***~4567");
        assertThat(StringMasker.maskMiddleOfString("123456789012345678"))
                .isEqualTo("1234~***~5678");
    }
}
