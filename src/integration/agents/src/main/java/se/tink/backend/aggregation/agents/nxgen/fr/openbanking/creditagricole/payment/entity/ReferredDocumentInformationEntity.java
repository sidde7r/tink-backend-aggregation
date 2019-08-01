package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.payment.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ReferredDocumentInformationEntity {
    @JsonProperty("type")
    private ReferredDocumentTypeEntity type = null;

    @JsonProperty("number")
    private String number = null;

    @JsonProperty("relatedDate")
    private LocalDate relatedDate = null;

    @JsonProperty("lineDetails")
    private List<LineDetailEntity> lineDetails = null;

    public ReferredDocumentTypeEntity getType() {
        return type;
    }

    public void setType(ReferredDocumentTypeEntity type) {
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

    public List<LineDetailEntity> getLineDetails() {
        return lineDetails;
    }

    public void setLineDetails(List<LineDetailEntity> lineDetails) {
        this.lineDetails = lineDetails;
    }
}
