package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DocumentAdjustmentEntity {
    @JsonProperty("amount")
    private AmountTypeEntity amount = null;

    @JsonProperty("creditDebitIndicator")
    private CreditDebitIndicatorEntity creditDebitIndicator = null;

    @JsonProperty("reason")
    private String reason = null;

    @JsonProperty("additionalInformation")
    private String additionalInformation = null;

    public AmountTypeEntity getAmount() {
        return amount;
    }

    public void setAmount(AmountTypeEntity amount) {
        this.amount = amount;
    }

    public CreditDebitIndicatorEntity getCreditDebitIndicator() {
        return creditDebitIndicator;
    }

    public void setCreditDebitIndicator(CreditDebitIndicatorEntity creditDebitIndicator) {
        this.creditDebitIndicator = creditDebitIndicator;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getAdditionalInformation() {
        return additionalInformation;
    }

    public void setAdditionalInformation(String additionalInformation) {
        this.additionalInformation = additionalInformation;
    }
}
