package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.executor.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.transfer.entities.TransferRequest;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PaymentRequest extends TransferRequest {
    @JsonProperty("ReferenceType")
    private String referenceType;

    public String getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(String referenceType) {
        this.referenceType = referenceType;
    }
}
