package se.tink.backend.aggregation.agents.banks.se.collector.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.PlusGiroIdentifier;

@JsonIgnoreProperties(ignoreUnknown = true)
class DepositInfo {
    private String ocr;
    private String plusGiro;

    public String getOcr() {
        return ocr;
    }

    @JsonProperty("ocrReference")
    public void setOcr(String ocr) {
        this.ocr = ocr;
    }

    public String getPlusGiro() {
        return plusGiro;
    }

    public void setPlusGiro(String plusGiro) {
        this.plusGiro = plusGiro;
    }

    @JsonProperty("OcrReference")
    public void setOcrCaps(String ocr) {
        this.ocr = ocr;
    }

    @JsonProperty("plusGiro")
    public void setPlusGiroCaps(String plusGiro) {
        this.plusGiro = plusGiro;
    }

    public AccountIdentifier toIdentifier() {
        return new PlusGiroIdentifier(plusGiro, ocr);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("OcrReference", ocr)
                .add("PlusGiro", plusGiro)
                .toString();
    }
}
