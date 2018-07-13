package se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import se.tink.libraries.giro.validation.OcrValidationConfiguration;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OcrCheckEntity {
    private Integer refLength1;
    private Integer refLength2;
    private OcrCheckType type;

    public Integer getRefLength1() {
        return refLength1;
    }

    public void setRefLength1(Integer refLength1) {
        this.refLength1 = refLength1;
    }

    public Integer getRefLength2() {
        return refLength2;
    }

    public void setRefLength2(Integer refLength2) {
        this.refLength2 = refLength2;
    }

    public OcrCheckType getType() {
        return type;
    }

    public void setType(OcrCheckType type) {
        this.type = type;
    }

    /**
     * Got these from the APK dump of Handelsbanken Android app
     */
    public enum OcrCheckType {
        MESSAGE,
        MESSAGE_OR_OCR,
        OCR_WITH_CONTROL_DIGIT,
        OCR_VARIABLE_LENGTH,
        OCR_FIXED_LENGTH
    }

    /**
     * Convenience creation of message validator
     */
    @JsonIgnore
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        OcrCheckEntity that = (OcrCheckEntity) o;

        return Objects.equal(this.refLength1, that.refLength1) &&
                Objects.equal(this.refLength2, that.refLength2) &&
                Objects.equal(this.type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(refLength1, refLength2, type);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("refLength1", refLength1)
                .add("refLength2", refLength2)
                .add("type", type)
                .toString();
    }
}
