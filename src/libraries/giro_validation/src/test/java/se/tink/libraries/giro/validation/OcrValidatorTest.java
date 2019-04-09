package se.tink.libraries.giro.validation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

@RunWith(Enclosed.class)
public class OcrValidatorTest {
    public static class Common {
        private static final OcrValidationConfiguration CONFIGURATION =
                OcrValidationConfiguration.noOcr();

        @Test
        public void numericIsPotentiallyIntendedOcr() {
            OcrValidator ocrValidator = new OcrValidator(CONFIGURATION);

            boolean nonNumericIsNotIntendedToBeOcr =
                    ocrValidator.isNumericPotentiallyIntentedOcr("1234");

            assertThat(nonNumericIsNotIntendedToBeOcr).isTrue();
        }

        @Test
        public void nonNumericIsNotPotentiallyIntendedOcr() {
            OcrValidator ocrValidator = new OcrValidator(CONFIGURATION);

            boolean nonNumericIsNotIntendedToBeOcr =
                    ocrValidator.isNumericPotentiallyIntentedOcr("_");

            assertThat(nonNumericIsNotIntendedToBeOcr).isFalse();
        }
    }

    public static class NoOcrValidationLevelTest {
        private static final OcrValidationConfiguration CONFIGURATION =
                OcrValidationConfiguration.noOcr();

        @Test
        public void nonOcrIsNotValid() {
            OcrValidator ocrValidator = new OcrValidator(CONFIGURATION);

            boolean valid = ocrValidator.isValid("_");

            assertThat(valid).isFalse();
        }

        @Test
        public void ocrIsNotValid() {
            OcrValidator ocrValidator = new OcrValidator(CONFIGURATION);

            boolean valid = ocrValidator.isValid("3776011373110074");

            assertThat(valid).isFalse();
        }
    }

    public static class SoftOcrValidationLevelTest {
        private static final OcrValidationConfiguration CONFIGURATION =
                OcrValidationConfiguration.softOcr();

        @Test
        public void nonNumericIsNotAValidOCR() {
            OcrValidator ocrValidator = new OcrValidator(CONFIGURATION);

            boolean valid = ocrValidator.isValid("TEST1234");

            assertThat(valid).isFalse();
        }

        @Test
        public void ocrMessageIsValid() {
            OcrValidator ocrValidator = new OcrValidator(CONFIGURATION);

            boolean valid = ocrValidator.isValid("3776011373110074");

            assertThat(valid).isTrue();
        }

        @Test
        public void incorrectOcrMessageIsInvalid() {
            OcrValidator ocrValidator = new OcrValidator(CONFIGURATION);

            boolean valid = ocrValidator.isValid("3776011373110070");

            assertThat(valid).isFalse();
        }

        @Test
        public void validationDisregardsSpacesInValidOcr() {
            OcrValidator ocrValidator = new OcrValidator(CONFIGURATION);

            boolean valid = ocrValidator.isValid("  3776 0113 7311 0074  ");

            assertThat(valid).isTrue();
        }
    }

    public static class HardOcrVariableLengthValidationTest {
        private static final OcrValidationConfiguration CONFIGURATION =
                OcrValidationConfiguration.hardOcrVariableLength();

        @Test
        public void ocrMessageIsValid() {
            OcrValidator ocrValidator = new OcrValidator(CONFIGURATION);

            boolean valid = ocrValidator.isValid("1212121212");

            assertThat(valid).isTrue();
        }

        @Test
        public void incorrectOcrMessageIsInvalid() {
            OcrValidator ocrValidator = new OcrValidator(CONFIGURATION);

            boolean valid = ocrValidator.isValid("3776011373110070");

            assertThat(valid).isFalse();
        }
    }

    /**
     * The Luhn digit is correct for the OCR, but the digit before the last digit should be
     * calculated as: ocr.length() % 10
     */
    public static class HardOcrVariableLengthWithLengthDigitValidationTest {
        private static final OcrValidationConfiguration CONFIGURATION =
                OcrValidationConfiguration.hardOcrVariableLengthWithLengthCheck();

        @Test
        public void ocrMessageWithCorrectLengthDigitIsValid() {
            OcrValidator ocrValidator = new OcrValidator(CONFIGURATION);

            boolean valid =
                    ocrValidator.isValid(
                            "41512178467811674"); // Valid Luhn ("4"), valid length digit ("7")

            assertThat(valid).isTrue();
        }

        @Test
        public void ocrMessageWithIncorrectLengthDigitIsInvalid() {
            OcrValidator ocrValidator = new OcrValidator(CONFIGURATION);

            boolean valid =
                    ocrValidator.isValid("1212121212"); // Valid Luhn, but length digit is wrong

            assertThat(valid).isFalse();
        }

        @Test
        public void incorrectOcrMessageIsInvalid() {
            OcrValidator ocrValidator = new OcrValidator(CONFIGURATION);

            boolean valid = ocrValidator.isValid("3776011373110070");

            assertThat(valid).isFalse();
        }
    }

    public static class HardOcrFixedLengthValidationTest {
        private static final OcrValidationConfiguration CONFIGURATION =
                OcrValidationConfiguration.hardOcrFixedLength(10);

        @Test
        public void ocrMessageWithCorrectFixedLengthIsValid() {
            OcrValidator ocrValidator = new OcrValidator(CONFIGURATION);

            boolean valid = ocrValidator.isValid("1212121212");

            assertThat(valid).isTrue();
        }

        @Test
        public void ocrMessageWithIncorrectFixedLengthIsInvalid() {
            OcrValidator ocrValidator = new OcrValidator(CONFIGURATION);

            boolean valid = ocrValidator.isValid("377601137311074"); // Not length of 10

            assertThat(valid).isFalse();
        }

        @Test
        public void ocrMessageWithAlternativeFixedLengthIsValid() {
            OcrValidationConfiguration configuration =
                    OcrValidationConfiguration.hardOcrFixedLength(10, 15);
            OcrValidator ocrValidator = new OcrValidator(configuration);

            boolean valid =
                    ocrValidator.isValid("377601137311074"); // Has alternative ocr length 15

            assertThat(valid).isFalse();
        }

        @Test
        public void incorrectOcrMessageIsInvalid() {
            OcrValidator ocrValidator = new OcrValidator(CONFIGURATION);

            boolean valid = ocrValidator.isValid("3776011373110070");

            assertThat(valid).isFalse();
        }
    }
}
