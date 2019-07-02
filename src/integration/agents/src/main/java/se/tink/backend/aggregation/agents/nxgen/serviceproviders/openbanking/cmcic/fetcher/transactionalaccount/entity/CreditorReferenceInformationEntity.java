package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreditorReferenceInformationEntity {
    @JsonProperty("type")
    private CodeAndIssuerEntity type = null;

    @JsonProperty("reference")
    private String reference = null;

    public CodeAndIssuerEntity getType() {
        return type;
    }

    public void setType(CodeAndIssuerEntity type) {
        this.type = type;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }
}
