package se.tink.libraries.giro.validation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class LuhnCheckTest {
    @Test
    public void calculatesCorrectCheck() {
        // A correct OCR: "41512178467811674"

        int calculatedCheck = LuhnCheck.calculateLuhnMod10Check("4151217846781167");
        int actualCheck = 4;

        assertThat(calculatedCheck).isEqualTo(actualCheck);
    }

    @Test
    public void isValidLastDigitForCorrectOcr() {
        boolean isValid = LuhnCheck.isLastCharCorrectLuhnMod10Check("41512178467811674");

        assertThat(isValid).isTrue();
    }

    @Test
    public void isNotValidForAllWrongChecksInOcr() {
        boolean isValid0 = LuhnCheck.isLastCharCorrectLuhnMod10Check("41512178467811670");
        boolean isValid1 = LuhnCheck.isLastCharCorrectLuhnMod10Check("41512178467811671");
        boolean isValid2 = LuhnCheck.isLastCharCorrectLuhnMod10Check("41512178467811672");
        boolean isValid3 = LuhnCheck.isLastCharCorrectLuhnMod10Check("41512178467811673");

        boolean isValid5 = LuhnCheck.isLastCharCorrectLuhnMod10Check("41512178467811675");
        boolean isValid6 = LuhnCheck.isLastCharCorrectLuhnMod10Check("41512178467811676");
        boolean isValid7 = LuhnCheck.isLastCharCorrectLuhnMod10Check("41512178467811677");
        boolean isValid8 = LuhnCheck.isLastCharCorrectLuhnMod10Check("41512178467811678");
        boolean isValid9 = LuhnCheck.isLastCharCorrectLuhnMod10Check("41512178467811679");

        assertThat(isValid0).isFalse();
        assertThat(isValid1).isFalse();
        assertThat(isValid2).isFalse();
        assertThat(isValid3).isFalse();

        assertThat(isValid5).isFalse();
        assertThat(isValid6).isFalse();
        assertThat(isValid7).isFalse();
        assertThat(isValid8).isFalse();
        assertThat(isValid9).isFalse();
    }
}
