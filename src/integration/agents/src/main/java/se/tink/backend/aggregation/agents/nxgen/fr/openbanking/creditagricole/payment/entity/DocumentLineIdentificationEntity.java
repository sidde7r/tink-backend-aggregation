package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.payment.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DocumentLineIdentificationEntity {
    @JsonProperty("type")
    private CodeAndIssuerEntity type = null;

    @JsonProperty("number")
    private String number = null;

    @JsonProperty("relatedDate")
    private LocalDate relatedDate = null;

    public CodeAndIssuerEntity getType() {
        return type;
    }

    public void setType(CodeAndIssuerEntity type) {
        this.type = type;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public LocalDate getRelatedDate() {
        return relatedDate;
    }

    public void setRelatedDate(LocalDate relatedDate) {
        this.relatedDate = relatedDate;
    }
}
