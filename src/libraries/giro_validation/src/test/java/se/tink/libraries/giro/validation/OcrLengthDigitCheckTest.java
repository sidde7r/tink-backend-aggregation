package se.tink.libraries.giro.validation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class OcrLengthDigitCheckTest {
    @Test
    public void isCorrectForOcrWithLengthCheck() {
        boolean isValid = OcrLengthDigitCheck.isValidLengthCheck("41512178467811674");

        assertThat(isValid).isTrue();
    }

    @Test
    public void isNotCorrectForOcrWithIncorrectLengthCheck() {
        boolean isValid0 = OcrLengthDigitCheck.isValidLengthCheck("41512178467811604");
        boolean isValid1 = OcrLengthDigitCheck.isValidLengthCheck("41512178467811614");
        boolean isValid2 = OcrLengthDigitCheck.isValidLengthCheck("41512178467811624");
        boolean isValid3 = OcrLengthDigitCheck.isValidLengthCheck("41512178467811634");
        boolean isValid4 = OcrLengthDigitCheck.isValidLengthCheck("41512178467811644");
        boolean isValid5 = OcrLengthDigitCheck.isValidLengthCheck("41512178467811654");
        boolean isValid6 = OcrLengthDigitCheck.isValidLengthCheck("41512178467811664");

        boolean isValid8 = OcrLengthDigitCheck.isValidLengthCheck("41512178467811684");
        boolean isValid9 = OcrLengthDigitCheck.isValidLengthCheck("41512178467811694");

        assertThat(isValid0).isFalse();
        assertThat(isValid1).isFalse();
        assertThat(isValid2).isFalse();
        assertThat(isValid3).isFalse();
        assertThat(isValid4).isFalse();
        assertThat(isValid5).isFalse();
        assertThat(isValid6).isFalse();
        assertThat(isValid8).isFalse();
        assertThat(isValid9).isFalse();
    }
}
