package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LineDetailEntity {
    @JsonProperty("identification")
    private DocumentLineIdentificationEntity identification = null;

    @JsonProperty("description")
    private String description = null;

    @JsonProperty("amount")
    private RemittanceAmountEntity amount = null;

    public DocumentLineIdentificationEntity getIdentification() {
        return identification;
    }

    public void setIdentification(DocumentLineIdentificationEntity identification) {
        this.identification = identification;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public RemittanceAmountEntity getAmount() {
        return amount;
    }

    public void setAmount(RemittanceAmountEntity amount) {
        this.amount = amount;
    }
}
