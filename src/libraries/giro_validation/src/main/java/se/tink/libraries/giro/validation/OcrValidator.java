package se.tink.libraries.giro.validation;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import javax.annotation.Nullable;
import org.apache.commons.lang3.StringUtils;

public class OcrValidator {
    private final OcrValidationLevel ocrValidationLevel;
    private final Integer ocrLength;
    @Nullable private final Integer ocrLengthAlt;

    public OcrValidator(OcrValidationConfiguration configuration) {
        this.ocrValidationLevel = configuration.getOcrValidationLevel();
        this.ocrLength = configuration.getOcrLength();
        this.ocrLengthAlt = configuration.getOcrLengthAlt();
    }

    public boolean isNumericPotentiallyIntentedOcr(String string) {
        return StringUtils.isNumeric(string);
    }

    public boolean isValid(String ocr) {

        if (ocr == null) {
            return false;
        }

        // Disregard spaces in the validation.
        String ocrWithoutSpaces = ocr.replaceAll("\\s", "");

        // Length check as per bankgiro specs(It must contain 2 to 25 digits including the check
        // digit and, if used, a length digit)
        // https://www.bankgirot.se/globalassets/dokument/anvandarmanualer/bankgiroreceivables_bankgiroinbetalningar_usermanaual_en.pdf
        if (ocrWithoutSpaces.length() < 2 || ocrWithoutSpaces.length() > 25) {
            return false;
        }

        if (Objects.equal(ocrValidationLevel, OcrValidationLevel.NO_OCR)) {
            return false;
        }

        if (!isNumericPotentiallyIntentedOcr(ocrWithoutSpaces)) {
            return false;
        }

        boolean isValidCheck = isValidLuhnCheck(ocrWithoutSpaces);

        switch (ocrValidationLevel) {
            case OCR_3_HARD: // OCR has length check built into the OCR number
                isValidCheck = isValidCheck && isValidLengthCheck(ocrWithoutSpaces);
                break;
            case OCR_4_HARD: // OCR required to be one of two possible fixed lengths
                Preconditions.checkArgument(
                        ocrLength != null,
                        "For hard fixed length validation at least one length is required.");
                isValidCheck = isValidCheck && isOneOfFixedLengths(ocrWithoutSpaces);
                break;
            case OCR_1_SOFT:
            case OCR_2_HARD:
                // only Luhn check
                break;
            case NO_OCR:
                // should never happen
                return false;
        }

        return isValidCheck;
    }

    private boolean isValidLuhnCheck(String ocr) {
        return LuhnCheck.isLastCharCorrectLuhnMod10Check(ocr);
    }

    private boolean isValidLengthCheck(String ocrWithLengthCheckDigit) {
        return OcrLengthDigitCheck.isValidLengthCheck(ocrWithLengthCheckDigit);
    }

    private boolean isOneOfFixedLengths(String ocr) {
        return Objects.equal(ocr.length(), ocrLength) || Objects.equal(ocr.length(), ocrLengthAlt);
    }
}
