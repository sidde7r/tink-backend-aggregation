package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RemittanceInformationStructuredEntity {
    private String referenceType;
    private String reference;

    @JsonIgnore
    public RemittanceInformationStructuredEntity createOCRRemittanceInformation(String reference) {
        this.referenceType = "OCR";
        this.reference = reference;
        return this;
    }
}
