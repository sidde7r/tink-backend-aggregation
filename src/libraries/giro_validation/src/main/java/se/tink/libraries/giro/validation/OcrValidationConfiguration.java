package se.tink.libraries.giro.validation;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import javax.annotation.Nullable;

public class OcrValidationConfiguration {
    private final OcrValidationLevel ocrValidationLevel;
    private final Integer ocrLength;
    @Nullable private final Integer ocrLengthAlt;

    public static OcrValidationConfiguration noOcr() {
        return new OcrValidationConfiguration(OcrValidationLevel.NO_OCR, null, null);
    }

    public static OcrValidationConfiguration softOcr() {
        return new OcrValidationConfiguration(OcrValidationLevel.OCR_1_SOFT, null, null);
    }

    public static OcrValidationConfiguration hardOcrVariableLength() {
        return new OcrValidationConfiguration(OcrValidationLevel.OCR_2_HARD, null, null);
    }

    public static OcrValidationConfiguration hardOcrVariableLengthWithLengthCheck() {
        return new OcrValidationConfiguration(OcrValidationLevel.OCR_3_HARD, null, null);
    }

    public static OcrValidationConfiguration hardOcrFixedLength(Integer ocrLength) {
        return hardOcrFixedLength(ocrLength, null);
    }

    public static OcrValidationConfiguration hardOcrFixedLength(
            Integer ocrLength, Integer ocrLengthAlt) {
        return new OcrValidationConfiguration(
                OcrValidationLevel.OCR_4_HARD, ocrLength, ocrLengthAlt);
    }

    private OcrValidationConfiguration(
            OcrValidationLevel ocrValidationLevel,
            Integer ocrLength,
            @Nullable Integer ocrLengthAlt) {
        this.ocrValidationLevel = ocrValidationLevel;
        this.ocrLength = ocrLength;
        this.ocrLengthAlt = ocrLengthAlt;
    }

    public OcrValidationLevel getOcrValidationLevel() {
        return ocrValidationLevel;
    }

    public Integer getOcrLength() {
        return ocrLength;
    }

    public Integer getOcrLengthAlt() {
        return ocrLengthAlt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OcrValidationConfiguration that = (OcrValidationConfiguration) o;
        return ocrValidationLevel == that.ocrValidationLevel
                && Objects.equal(ocrLength, that.ocrLength)
                && Objects.equal(ocrLengthAlt, that.ocrLengthAlt);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(ocrValidationLevel, ocrLength, ocrLengthAlt);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("ocrValidationLevel", ocrValidationLevel)
                .add("ocrLength", ocrLength)
                .add("ocrLengthAlt", ocrLengthAlt)
                .toString();
    }
}
