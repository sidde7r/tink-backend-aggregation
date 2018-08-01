package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.entities;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.giro.validation.OcrValidationConfiguration;

@JsonObject
public class OcrCheck {
    private OcrCheckType type;
    private int refLength1;
    private int refLength2;

    public enum OcrCheckType {
        MESSAGE,
        MESSAGE_OR_OCR,
        OCR_WITH_CONTROL_DIGIT,
        OCR_VARIABLE_LENGTH,
        OCR_FIXED_LENGTH
    }

    public OcrValidationConfiguration getValidationConfiguration() {
        switch (type) {
        case MESSAGE:
            return OcrValidationConfiguration.noOcr();
        case MESSAGE_OR_OCR:
            return OcrValidationConfiguration.softOcr();
        case OCR_WITH_CONTROL_DIGIT:
            return OcrValidationConfiguration.hardOcrVariableLength();
        case OCR_VARIABLE_LENGTH:
            return OcrValidationConfiguration.hardOcrVariableLengthWithLengthCheck();
        case OCR_FIXED_LENGTH:
            return OcrValidationConfiguration.hardOcrFixedLength(refLength1, refLength2);
        default:
            return null;
        }
    }
}

