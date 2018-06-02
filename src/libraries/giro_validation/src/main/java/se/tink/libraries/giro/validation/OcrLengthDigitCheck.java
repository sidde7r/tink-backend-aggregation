package se.tink.libraries.giro.validation;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;

public class OcrLengthDigitCheck {
    public static boolean isValidLengthCheck(String ocr) {
        Preconditions.checkArgument(StringUtils.isNumeric(ocr));

        int calculatedLengthCheck = ocr.length() % 10;

        int lengthCheckPosition = ocr.length() - 2;
        String actualLengthCheckChar = ocr.substring(lengthCheckPosition, lengthCheckPosition + 1);
        int actualLengthCheck = Integer.parseInt(actualLengthCheckChar);

        return Objects.equal(calculatedLengthCheck, actualLengthCheck);
    }
}
