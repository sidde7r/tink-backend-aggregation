package se.tink.backend.aggregation.agents.utils.giro.validation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import se.tink.libraries.giro.validation.OcrValidationConfiguration;

@RunWith(Enclosed.class)
public class GiroMessageValidatorTest {
    public static class CommonTest {
        @Test
        public void noMessageIsInvalid() {
            se.tink.backend.aggregation.agents.utils.giro.validation.GiroMessageValidator
                    giroMessageValidator =
                            se.tink.backend.aggregation.agents.utils.giro.validation
                                    .GiroMessageValidator.create(
                                    OcrValidationConfiguration.noOcr());

            se.tink.backend.aggregation.agents.utils.giro.validation.GiroMessageValidator
                            .ValidationResult
                    resultNull = giroMessageValidator.validate(null);
            se.tink.backend.aggregation.agents.utils.giro.validation.GiroMessageValidator
                            .ValidationResult
                    resultEmpty = giroMessageValidator.validate("");

            assertThat(resultNull.getValidMessage().isPresent()).isFalse();
            assertThat(resultEmpty.getValidMessage().isPresent()).isFalse();
            assertThat(resultEmpty.isNumericPotentiallyIntentedOcr()).isFalse();
            assertThat(resultNull.isNumericPotentiallyIntentedOcr()).isFalse();
        }
    }

    public static class NoOcrValidationLevelTest {

        private static final OcrValidationConfiguration CONFIGURATION =
                OcrValidationConfiguration.noOcr();

        @Test
        public void hasExpectedAllowedType() {
            se.tink.backend.aggregation.agents.utils.giro.validation.GiroMessageValidator
                    giroMessageValidator =
                            se.tink.backend.aggregation.agents.utils.giro.validation
                                    .GiroMessageValidator.create(CONFIGURATION);

            se.tink.backend.aggregation.agents.utils.giro.validation.GiroMessageValidator
                            .ValidationResult
                    validate = giroMessageValidator.validate("_");

            assertThat(validate.getAllowedType())
                    .isEqualTo(
                            se.tink.backend.aggregation.agents.utils.giro.validation
                                    .GiroMessageValidator.AllowedType.MESSAGE);
        }

        @Test
        public void oneCharacterNumericShouldNotThrowNpe() {
            se.tink.backend.aggregation.agents.utils.giro.validation.GiroMessageValidator
                    giroMessageValidator =
                            se.tink.backend.aggregation.agents.utils.giro.validation
                                    .GiroMessageValidator.create(
                                    OcrValidationConfiguration.softOcr());

            se.tink.backend.aggregation.agents.utils.giro.validation.GiroMessageValidator
                            .ValidationResult
                    validate = giroMessageValidator.validate("5");
            assertThat(validate.getValidOcr()).isEmpty();
        }

        @Test
        public void nonEmptyMessageIsValidAsMessage() {
            se.tink.backend.aggregation.agents.utils.giro.validation.GiroMessageValidator
                    giroMessageValidator =
                            se.tink.backend.aggregation.agents.utils.giro.validation
                                    .GiroMessageValidator.create(CONFIGURATION);

            se.tink.backend.aggregation.agents.utils.giro.validation.GiroMessageValidator
                            .ValidationResult
                    validate = giroMessageValidator.validate("Some messageåäö+01");

            assertThat(validate.getValidMessage().isPresent()).isTrue();
            assertThat(validate.getValidOcr().isPresent()).isFalse();
            assertThat(validate.getValidMessage().get()).isEqualTo("Some messageåäö+01");
            assertThat(validate.isNumericPotentiallyIntentedOcr()).isFalse();
        }

        @Test
        public void ocrLookingMessageIsOnlyAMessage() {
            se.tink.backend.aggregation.agents.utils.giro.validation.GiroMessageValidator
                    giroMessageValidator =
                            se.tink.backend.aggregation.agents.utils.giro.validation
                                    .GiroMessageValidator.create(CONFIGURATION);

            se.tink.backend.aggregation.agents.utils.giro.validation.GiroMessageValidator
                            .ValidationResult
                    validate = giroMessageValidator.validate("3776011373110074");

            assertThat(validate.getValidMessage().isPresent()).isTrue();
            assertThat(validate.getValidOcr().isPresent()).isFalse();
            assertThat(validate.getValidMessage().get()).isEqualTo("3776011373110074");
            assertThat(validate.isNumericPotentiallyIntentedOcr()).isTrue();
        }
    }

    public static class SoftOcrValidationLevelTest {
        private static final OcrValidationConfiguration CONFIGURATION =
                OcrValidationConfiguration.softOcr();

        @Test
        public void hasExpectedAllowedType() {
            se.tink.backend.aggregation.agents.utils.giro.validation.GiroMessageValidator
                    giroMessageValidator =
                            se.tink.backend.aggregation.agents.utils.giro.validation
                                    .GiroMessageValidator.create(CONFIGURATION);

            se.tink.backend.aggregation.agents.utils.giro.validation.GiroMessageValidator
                            .ValidationResult
                    validate = giroMessageValidator.validate("_");

            assertThat(validate.getAllowedType())
                    .isEqualTo(
                            se.tink.backend.aggregation.agents.utils.giro.validation
                                    .GiroMessageValidator.AllowedType.MESSAGE_OR_OCR);
        }

        @Test
        public void nonEmptyMessageIsValidMessage() {
            se.tink.backend.aggregation.agents.utils.giro.validation.GiroMessageValidator
                    giroMessageValidator =
                            se.tink.backend.aggregation.agents.utils.giro.validation
                                    .GiroMessageValidator.create(CONFIGURATION);

            se.tink.backend.aggregation.agents.utils.giro.validation.GiroMessageValidator
                            .ValidationResult
                    validate = giroMessageValidator.validate("Some messageåäö+01");

            assertThat(validate.getValidMessage().get()).isEqualTo("Some messageåäö+01");
            assertThat(validate.isNumericPotentiallyIntentedOcr()).isFalse();
        }

        @Test
        public void ocrMessageIsBothOcrAndMessage() {
            se.tink.backend.aggregation.agents.utils.giro.validation.GiroMessageValidator
                    giroMessageValidator =
                            se.tink.backend.aggregation.agents.utils.giro.validation
                                    .GiroMessageValidator.create(CONFIGURATION);

            se.tink.backend.aggregation.agents.utils.giro.validation.GiroMessageValidator
                            .ValidationResult
                    validate = giroMessageValidator.validate("3776011373110074");

            assertThat(validate.getValidMessage().get()).isEqualTo("3776011373110074");
            assertThat(validate.getValidOcr().get()).isEqualTo("3776011373110074");
            assertThat(validate.isNumericPotentiallyIntentedOcr()).isTrue();
        }

        @Test
        public void incorrectOcrMessageIsOnlyAMessage() {
            se.tink.backend.aggregation.agents.utils.giro.validation.GiroMessageValidator
                    giroMessageValidator =
                            se.tink.backend.aggregation.agents.utils.giro.validation
                                    .GiroMessageValidator.create(CONFIGURATION);

            se.tink.backend.aggregation.agents.utils.giro.validation.GiroMessageValidator
                            .ValidationResult
                    validate = giroMessageValidator.validate("3776011373110070");

            assertThat(validate.getValidMessage().get()).isEqualTo("3776011373110070");
            assertThat(validate.getValidOcr().isPresent()).isFalse();
            assertThat(validate.isNumericPotentiallyIntentedOcr()).isTrue();
        }
    }

    public static class HardOcrValidationTest {

        private static final OcrValidationConfiguration CONFIGURATION =
                OcrValidationConfiguration.hardOcrVariableLength();

        @Test
        public void hasExpectedAllowedType() {
            se.tink.backend.aggregation.agents.utils.giro.validation.GiroMessageValidator
                    giroMessageValidator =
                            se.tink.backend.aggregation.agents.utils.giro.validation
                                    .GiroMessageValidator.create(CONFIGURATION);

            se.tink.backend.aggregation.agents.utils.giro.validation.GiroMessageValidator
                            .ValidationResult
                    validate = giroMessageValidator.validate("_");

            assertThat(validate.getAllowedType())
                    .isEqualTo(
                            se.tink.backend.aggregation.agents.utils.giro.validation
                                    .GiroMessageValidator.AllowedType.OCR);
        }

        @Test
        public void nonEmptyMessageIsNotValid() {
            se.tink.backend.aggregation.agents.utils.giro.validation.GiroMessageValidator
                    giroMessageValidator =
                            se.tink.backend.aggregation.agents.utils.giro.validation
                                    .GiroMessageValidator.create(CONFIGURATION);

            se.tink.backend.aggregation.agents.utils.giro.validation.GiroMessageValidator
                            .ValidationResult
                    validate = giroMessageValidator.validate("Some messageåäö+01");

            assertThat(validate.getValidMessage().isPresent()).isFalse();
            assertThat(validate.isNumericPotentiallyIntentedOcr()).isFalse();
        }

        @Test
        public void ocrIsOnlyOcr() {
            se.tink.backend.aggregation.agents.utils.giro.validation.GiroMessageValidator
                    giroMessageValidator =
                            se.tink.backend.aggregation.agents.utils.giro.validation
                                    .GiroMessageValidator.create(CONFIGURATION);

            se.tink.backend.aggregation.agents.utils.giro.validation.GiroMessageValidator
                            .ValidationResult
                    validate = giroMessageValidator.validate("3776011373110074");

            assertThat(validate.getValidMessage().isPresent()).isFalse();
            assertThat(validate.getValidOcr().get()).isEqualTo("3776011373110074");
            assertThat(validate.isNumericPotentiallyIntentedOcr()).isTrue();
        }

        @Test
        public void incorrectOcrMessageIsNeverValid() {
            se.tink.backend.aggregation.agents.utils.giro.validation.GiroMessageValidator
                    giroMessageValidator =
                            se.tink.backend.aggregation.agents.utils.giro.validation
                                    .GiroMessageValidator.create(CONFIGURATION);

            se.tink.backend.aggregation.agents.utils.giro.validation.GiroMessageValidator
                            .ValidationResult
                    validate = giroMessageValidator.validate("3776011373110070");

            assertThat(validate.getValidMessage().isPresent()).isFalse();
            assertThat(validate.getValidOcr().isPresent()).isFalse();
            assertThat(validate.isNumericPotentiallyIntentedOcr()).isTrue();
        }
    }

    public static class Examples {
        @Test
        public void noOcrValidation_ValidMessageOnly() {
            OcrValidationConfiguration noOcrConfiguration = OcrValidationConfiguration.noOcr();
            String validMessage =
                    "3776011373110074"; // Is a valid OCR usually, but we configured not accepting
            // OCR

            se.tink.backend.aggregation.agents.utils.giro.validation.GiroMessageValidator
                    giroMessageValidator =
                            se.tink.backend.aggregation.agents.utils.giro.validation
                                    .GiroMessageValidator.create(noOcrConfiguration);

            se.tink.backend.aggregation.agents.utils.giro.validation.GiroMessageValidator
                            .ValidationResult
                    validate = giroMessageValidator.validate(validMessage);

            assertThat(validate.getAllowedType())
                    .isEqualTo(
                            se.tink.backend.aggregation.agents.utils.giro.validation
                                    .GiroMessageValidator.AllowedType.MESSAGE);
            assertThat(validate.getValidMessage().get()).isEqualTo("3776011373110074");
            assertThat(validate.getValidOcr().isPresent()).isFalse();
            assertThat(validate.isNumericPotentiallyIntentedOcr()).isTrue();
        }

        @Test
        public void softOcrValidation_ValidMessage() {
            OcrValidationConfiguration softOcrconfiguration = OcrValidationConfiguration.softOcr();
            String validOcr = "Lööl message";

            se.tink.backend.aggregation.agents.utils.giro.validation.GiroMessageValidator
                    giroMessageValidator =
                            se.tink.backend.aggregation.agents.utils.giro.validation
                                    .GiroMessageValidator.create(softOcrconfiguration);

            se.tink.backend.aggregation.agents.utils.giro.validation.GiroMessageValidator
                            .ValidationResult
                    validate = giroMessageValidator.validate(validOcr);

            assertThat(validate.getAllowedType())
                    .isEqualTo(
                            se.tink.backend.aggregation.agents.utils.giro.validation
                                    .GiroMessageValidator.AllowedType.MESSAGE_OR_OCR);
            assertThat(validate.getValidMessage().get()).isEqualTo("Lööl message");
            assertThat(validate.getValidOcr().isPresent()).isFalse();
            assertThat(validate.isNumericPotentiallyIntentedOcr()).isFalse();
        }

        @Test
        public void softOcrValidation_InvalidOcr_IsValidMessage() {
            OcrValidationConfiguration softOcrconfiguration = OcrValidationConfiguration.softOcr();
            String validOcr =
                    "1231011373110074"; // Since it doesn't validate with Luhn check, this is not
            // valid ocr

            se.tink.backend.aggregation.agents.utils.giro.validation.GiroMessageValidator
                    giroMessageValidator =
                            se.tink.backend.aggregation.agents.utils.giro.validation
                                    .GiroMessageValidator.create(softOcrconfiguration);

            se.tink.backend.aggregation.agents.utils.giro.validation.GiroMessageValidator
                            .ValidationResult
                    validate = giroMessageValidator.validate(validOcr);

            assertThat(validate.getAllowedType())
                    .isEqualTo(
                            se.tink.backend.aggregation.agents.utils.giro.validation
                                    .GiroMessageValidator.AllowedType.MESSAGE_OR_OCR);
            assertThat(validate.getValidMessage().get()).isEqualTo("1231011373110074");
            assertThat(validate.getValidOcr().isPresent()).isFalse();
            assertThat(validate.isNumericPotentiallyIntentedOcr()).isTrue();
        }

        @Test
        public void softOcrValidation_ValidOcr_IsMessageAndOcr() {
            OcrValidationConfiguration softOcrconfiguration = OcrValidationConfiguration.softOcr();
            String validOcr =
                    "3776011373110074"; // Since soft this can be both an OCR and a message

            se.tink.backend.aggregation.agents.utils.giro.validation.GiroMessageValidator
                    giroMessageValidator =
                            se.tink.backend.aggregation.agents.utils.giro.validation
                                    .GiroMessageValidator.create(softOcrconfiguration);

            se.tink.backend.aggregation.agents.utils.giro.validation.GiroMessageValidator
                            .ValidationResult
                    validate = giroMessageValidator.validate(validOcr);

            assertThat(validate.getAllowedType())
                    .isEqualTo(
                            se.tink.backend.aggregation.agents.utils.giro.validation
                                    .GiroMessageValidator.AllowedType.MESSAGE_OR_OCR);
            assertThat(validate.getValidMessage().get()).isEqualTo("3776011373110074");
            assertThat(validate.getValidOcr().get()).isEqualTo("3776011373110074");
            assertThat(validate.isNumericPotentiallyIntentedOcr()).isTrue();
        }

        @Test
        public void hardValidationVariableLength_ValidOcr() {
            OcrValidationConfiguration hardOcrVariableLength =
                    OcrValidationConfiguration.hardOcrVariableLength();
            String validOcr = "3776011373110074";

            se.tink.backend.aggregation.agents.utils.giro.validation.GiroMessageValidator
                    giroMessageValidator =
                            se.tink.backend.aggregation.agents.utils.giro.validation
                                    .GiroMessageValidator.create(hardOcrVariableLength);

            se.tink.backend.aggregation.agents.utils.giro.validation.GiroMessageValidator
                            .ValidationResult
                    validate = giroMessageValidator.validate(validOcr);

            assertThat(validate.getAllowedType())
                    .isEqualTo(
                            se.tink.backend.aggregation.agents.utils.giro.validation
                                    .GiroMessageValidator.AllowedType.OCR);
            assertThat(validate.getValidMessage().isPresent()).isFalse();
            assertThat(validate.getValidOcr().get()).isEqualTo("3776011373110074");
            assertThat(validate.isNumericPotentiallyIntentedOcr()).isTrue();
        }

        @Test
        public void hardValidationVariableLength_InvalidOcr() {
            OcrValidationConfiguration hardOcrVariableLength =
                    OcrValidationConfiguration.hardOcrVariableLength();
            String validOcr = "1231011373110074";

            se.tink.backend.aggregation.agents.utils.giro.validation.GiroMessageValidator
                    giroMessageValidator =
                            se.tink.backend.aggregation.agents.utils.giro.validation
                                    .GiroMessageValidator.create(hardOcrVariableLength);

            se.tink.backend.aggregation.agents.utils.giro.validation.GiroMessageValidator
                            .ValidationResult
                    validate = giroMessageValidator.validate(validOcr);

            assertThat(validate.getAllowedType())
                    .isEqualTo(
                            se.tink.backend.aggregation.agents.utils.giro.validation
                                    .GiroMessageValidator.AllowedType.OCR);
            assertThat(validate.getValidMessage().isPresent()).isFalse();
            assertThat(validate.getValidOcr().isPresent()).isFalse();
            assertThat(validate.isNumericPotentiallyIntentedOcr()).isTrue();
        }

        @Test
        public void hardValidationVariableLengthWithLengthCheck_ValidOcr_CorrectDigit() {
            OcrValidationConfiguration hardOcrVariableLengthWithLengthCheck =
                    OcrValidationConfiguration.hardOcrVariableLengthWithLengthCheck();
            String validOcrWrongDigit =
                    "41512178467811674"; // "7" is % 10 of string length (17 % 10 = 7)

            se.tink.backend.aggregation.agents.utils.giro.validation.GiroMessageValidator
                    giroMessageValidator =
                            se.tink.backend.aggregation.agents.utils.giro.validation
                                    .GiroMessageValidator.create(
                                    hardOcrVariableLengthWithLengthCheck);

            se.tink.backend.aggregation.agents.utils.giro.validation.GiroMessageValidator
                            .ValidationResult
                    validate = giroMessageValidator.validate(validOcrWrongDigit);

            assertThat(validate.getAllowedType())
                    .isEqualTo(
                            se.tink.backend.aggregation.agents.utils.giro.validation
                                    .GiroMessageValidator.AllowedType.OCR);
            assertThat(validate.getValidMessage().isPresent()).isFalse();
            assertThat(validate.getValidOcr().get()).isEqualTo("41512178467811674");
            assertThat(validate.isNumericPotentiallyIntentedOcr()).isTrue();
        }

        @Test
        public void hrdValidationVariableLengthWithLengthCheck_ValidOcr_WrongDigit() {
            OcrValidationConfiguration hardOcrVariableLengthWithLengthCheck =
                    OcrValidationConfiguration.hardOcrVariableLengthWithLengthCheck();
            String validOcrWrongDigit = "3776011373110074"; // "7" is not % 10 of string length

            se.tink.backend.aggregation.agents.utils.giro.validation.GiroMessageValidator
                    giroMessageValidator =
                            se.tink.backend.aggregation.agents.utils.giro.validation
                                    .GiroMessageValidator.create(
                                    hardOcrVariableLengthWithLengthCheck);

            se.tink.backend.aggregation.agents.utils.giro.validation.GiroMessageValidator
                            .ValidationResult
                    validate = giroMessageValidator.validate(validOcrWrongDigit);

            assertThat(validate.getAllowedType())
                    .isEqualTo(
                            se.tink.backend.aggregation.agents.utils.giro.validation
                                    .GiroMessageValidator.AllowedType.OCR);
            assertThat(validate.getValidMessage().isPresent()).isFalse();
            assertThat(validate.getValidOcr().isPresent()).isFalse();
            assertThat(validate.isNumericPotentiallyIntentedOcr()).isTrue();
        }

        @Test
        public void hardValidationFixedLengthWith_ValidOcr_CorrectLength() {
            OcrValidationConfiguration hardOcrFixedLengthConfiguration =
                    OcrValidationConfiguration.hardOcrFixedLength(
                            16); // Allows 16 or 10 char length valid OCRs
            String validOcrCorrectLength =
                    "3776011373110074"; // 16 char string is ok if Luhn is valid

            se.tink.backend.aggregation.agents.utils.giro.validation.GiroMessageValidator
                    giroMessageValidator =
                            se.tink.backend.aggregation.agents.utils.giro.validation
                                    .GiroMessageValidator.create(hardOcrFixedLengthConfiguration);

            se.tink.backend.aggregation.agents.utils.giro.validation.GiroMessageValidator
                            .ValidationResult
                    validate = giroMessageValidator.validate(validOcrCorrectLength);

            assertThat(validate.getAllowedType())
                    .isEqualTo(
                            se.tink.backend.aggregation.agents.utils.giro.validation
                                    .GiroMessageValidator.AllowedType.OCR);
            assertThat(validate.getValidMessage().isPresent()).isFalse();
            assertThat(validate.getValidOcr().get()).isEqualTo("3776011373110074");
            assertThat(validate.isNumericPotentiallyIntentedOcr()).isTrue();
        }

        @Test
        public void hardValidationFixedLengthWith_ValidOcr_CorrectAlternativeLength() {
            OcrValidationConfiguration hardOcrFixedLengthConfiguration =
                    OcrValidationConfiguration.hardOcrFixedLength(
                            16, 10); // Allows 16 or 10 char length valid OCRs
            String validOcrCorrectLength =
                    "1212121212"; // 10 char string is also ok if Luhn is valid

            se.tink.backend.aggregation.agents.utils.giro.validation.GiroMessageValidator
                    giroMessageValidator =
                            se.tink.backend.aggregation.agents.utils.giro.validation
                                    .GiroMessageValidator.create(hardOcrFixedLengthConfiguration);

            se.tink.backend.aggregation.agents.utils.giro.validation.GiroMessageValidator
                            .ValidationResult
                    validate = giroMessageValidator.validate(validOcrCorrectLength);

            assertThat(validate.getAllowedType())
                    .isEqualTo(
                            se.tink.backend.aggregation.agents.utils.giro.validation
                                    .GiroMessageValidator.AllowedType.OCR);
            assertThat(validate.getValidMessage().isPresent()).isFalse();
            assertThat(validate.getValidOcr().get()).isEqualTo("1212121212");
            assertThat(validate.isNumericPotentiallyIntentedOcr()).isTrue();
        }

        @Test
        public void hardValidationFixedLengthWith_ValidOcr_WrongLength() {
            OcrValidationConfiguration hardOcrFixedLengthConfiguration =
                    OcrValidationConfiguration.hardOcrFixedLength(
                            16, 10); // Allows 16 or 10 char length valid OCRs
            String validOcrCorrectLength =
                    "41512178467811674"; // 17 chars != 16 nor 10, altough valid Luhn check

            se.tink.backend.aggregation.agents.utils.giro.validation.GiroMessageValidator
                    giroMessageValidator =
                            se.tink.backend.aggregation.agents.utils.giro.validation
                                    .GiroMessageValidator.create(hardOcrFixedLengthConfiguration);

            se.tink.backend.aggregation.agents.utils.giro.validation.GiroMessageValidator
                            .ValidationResult
                    validate = giroMessageValidator.validate(validOcrCorrectLength);

            assertThat(validate.getAllowedType())
                    .isEqualTo(
                            se.tink.backend.aggregation.agents.utils.giro.validation
                                    .GiroMessageValidator.AllowedType.OCR);
            assertThat(validate.getValidMessage().isPresent()).isFalse();
            assertThat(validate.getValidOcr().isPresent()).isFalse();
            assertThat(validate.isNumericPotentiallyIntentedOcr()).isTrue();
        }
    }
}
