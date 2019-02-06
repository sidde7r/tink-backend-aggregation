package se.tink.backend.aggregation.agents.utils.giro.validation;

import com.google.common.base.Strings;
import com.google.common.base.Objects;
import java.util.Optional;
import se.tink.libraries.giro.validation.OcrValidationConfiguration;
import se.tink.libraries.giro.validation.OcrValidationLevel;
import se.tink.libraries.giro.validation.OcrValidator;

public class GiroMessageValidator {
    public enum AllowedType {
        MESSAGE_OR_OCR, MESSAGE, OCR
    }

    public static class ValidationResult {
        private AllowedType allowedType;
        private String validOcr;
        private String validMessage;
        private boolean numericPotentiallyIntentedOcr;

        public AllowedType getAllowedType() {
            return allowedType;
        }

        public void setAllowedType(AllowedType allowedType) {
            this.allowedType = allowedType;
        }

        public boolean isNumericPotentiallyIntentedOcr() {
            return numericPotentiallyIntentedOcr;
        }

        public void setNumericPotentiallyIntentedOcr(boolean numericPotentiallyIntentedOcr) {
            this.numericPotentiallyIntentedOcr = numericPotentiallyIntentedOcr;
        }

        public Optional<String> getValidMessage() {
            return Optional.ofNullable(validMessage);
        }

        public void setValidMessage(String validMessage) {
            this.validMessage = validMessage;
        }

        public Optional<String> getValidOcr() {
            return Optional.ofNullable(validOcr);
        }

        public void setValidOcr(String validOcr) {
            this.validOcr = validOcr;
        }
    }

    private final OcrValidator ocrValidator;
    private final OcrValidationLevel ocrValidationLevel;

    private GiroMessageValidator(OcrValidationConfiguration validationConfiguration) {
        this.ocrValidationLevel = validationConfiguration.getOcrValidationLevel();
        this.ocrValidator = new OcrValidator(validationConfiguration);
    }

    public static GiroMessageValidator create(OcrValidationConfiguration configuration) {
        return new GiroMessageValidator(configuration);
    }

    public ValidationResult validate(String message) {
        ValidationResult validationResult = new ValidationResult();
        validationResult.setAllowedType(getValidType());

        if (!Strings.isNullOrEmpty(message)) {
            if (isMessageAllowed()) {
                validationResult.setValidMessage(message);
            }

            if (isNumericPotentiallyIntentedOcr(message)) {
                validationResult.setNumericPotentiallyIntentedOcr(true);
            }

            if (isOcrAllowed() && isValidOcr(message)) {
                validationResult.setValidOcr(message);
            }
        }

        return validationResult;
    }

    private AllowedType getValidType() {
        switch (ocrValidationLevel) {
        case NO_OCR:
            return AllowedType.MESSAGE;
        case OCR_1_SOFT:
            return AllowedType.MESSAGE_OR_OCR;
        case OCR_2_HARD:
        case OCR_3_HARD:
        case OCR_4_HARD:
            return AllowedType.OCR;
        default:
            throw new IllegalStateException("Instantiated with an incorrect OCR type");
        }
    }

    private boolean isMessageAllowed() {
        return Objects.equal(ocrValidationLevel, OcrValidationLevel.NO_OCR) ||
                Objects.equal(ocrValidationLevel, OcrValidationLevel.OCR_1_SOFT);
    }

    private boolean isOcrAllowed() {
        return !Objects.equal(ocrValidationLevel, OcrValidationLevel.NO_OCR);
    }

    private boolean isValidOcr(String message) {
        return ocrValidator.isValid(message);
    }

    private boolean isNumericPotentiallyIntentedOcr(String message) {
        return ocrValidator.isNumericPotentiallyIntentedOcr(message);
    }
}
