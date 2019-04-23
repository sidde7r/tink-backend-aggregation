package se.tink.backend.aggregation.agents.banks.seb.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.utils.giro.validation.GiroMessageValidator;
import se.tink.libraries.giro.validation.OcrValidationConfiguration;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GiroEntity {
    @JsonProperty("ROW_ID")
    public String ROW_ID;

    @JsonProperty("PG_KONTO_NUMMER")
    public String PG_KONTO_NUMMER;

    @JsonProperty("PG_KONTO_HAV")
    public String PG_KONTO_HAV;

    @JsonProperty("PG_KONTO_OCR_FL")
    public String PG_KONTO_OCR_FL;

    @JsonProperty("PGKTO_OCR_LANGD")
    public String PGKTO_OCR_LANGD;

    @JsonProperty("PGKTO_OCR_ALTLANGD")
    public String PGKTO_OCR_ALTLANGD;

    @JsonProperty("BGCNR")
    public String BGCNR;

    @JsonProperty("KUND_NR_PERSORG")
    public Long KUND_NR_PERSORG;

    @JsonProperty("NAMN")
    public String NAMN;

    @JsonProperty("BG_KONTO_OCR_FL")
    public String BG_KONTO_OCR_FL;

    @JsonProperty("BG_KONTO_SKATT_FL")
    public String BG_KONTO_SKATT_FL;

    @JsonProperty("OCR_REF_LANGD1")
    public Integer OCR_REF_LANGD1;

    @JsonProperty("OCR_REF_LANGD2")
    public Integer OCR_REF_LANGD2;

    @JsonProperty("MOTT_CLNR")
    public String MOTT_CLNR;

    @JsonProperty("KONTONR")
    public String KONTONR;

    @JsonProperty("MOTT_CLNR_EUR")
    public String MOTT_CLNR_EUR;

    @JsonProperty("KONTONR_EUR")
    public String KONTONR_EUR;

    public boolean isSePg() {
        return !Strings.isNullOrEmpty(PG_KONTO_NUMMER);
    }

    public boolean isSeBg() {
        return !Strings.isNullOrEmpty(BGCNR);
    }

    private OcrValidationConfiguration getPgMessageValidatorConfiguration() {
        if (Objects.equal(PG_KONTO_OCR_FL, "")) {
            return OcrValidationConfiguration.noOcr();
        } else if (Objects.equal(PG_KONTO_OCR_FL, "O")) {
            Integer fixedLength = getIntegerOrNull(PGKTO_OCR_LANGD);
            Integer fixedLengthAlt = getIntegerOrNull(PGKTO_OCR_ALTLANGD);

            if (fixedLength != null && fixedLengthAlt != null) {
                return OcrValidationConfiguration.hardOcrFixedLength(fixedLength, fixedLengthAlt);
            } else if (fixedLength != null) {
                return OcrValidationConfiguration.hardOcrFixedLength(fixedLength);
            } else {
                return OcrValidationConfiguration.hardOcrVariableLength();
            }
        }

        throw new IllegalStateException(
                "Not possible to parse PG_KONTO_OCR_FL into OcrValidationLevel ("
                        + PG_KONTO_OCR_FL
                        + ")");
    }

    private Integer getIntegerOrNull(String string) {
        return !Strings.isNullOrEmpty(string) && StringUtils.isNumeric(string)
                ? Integer.parseInt(string)
                : null;
    }

    private OcrValidationConfiguration getBgMessageValidatorConfiguration() {
        switch (BG_KONTO_OCR_FL) {
            case "":
            case "0":
                return OcrValidationConfiguration.noOcr();
            case "1":
                return OcrValidationConfiguration.softOcr();
            case "2":
                return OcrValidationConfiguration.hardOcrVariableLength();
            case "3":
                return OcrValidationConfiguration.hardOcrVariableLengthWithLengthCheck();
            case "4":
                Integer fixedLength = OCR_REF_LANGD1;
                Integer fixedLengthAlt = OCR_REF_LANGD2;

                if (fixedLength != null && fixedLengthAlt != null) {
                    return OcrValidationConfiguration.hardOcrFixedLength(
                            fixedLength, fixedLengthAlt);
                } else if (fixedLength != null) {
                    return OcrValidationConfiguration.hardOcrFixedLength(fixedLength);
                } else {
                    throw new IllegalStateException(
                            "No valid ocr lengths for fixed type (both lengths null)");
                }
            default:
                throw new IllegalStateException(
                        "Not possible to parse BG_KONTO_OCR_FL into OcrValidationLevel ("
                                + BG_KONTO_OCR_FL
                                + ")");
        }
    }

    private OcrValidationConfiguration getOcrValidationConfiguration() {
        if (isSePg()) {
            return getPgMessageValidatorConfiguration();
        } else if (isSeBg()) {
            return getBgMessageValidatorConfiguration();
        }

        throw new IllegalStateException("Not a valid BG/PG type");
    }

    public GiroMessageValidator createMessageValidator() {
        return GiroMessageValidator.create(getOcrValidationConfiguration());
    }
}
