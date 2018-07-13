package se.tink.backend.aggregation.agents.banks.nordea.v20.model.payments.fi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import se.tink.backend.aggregation.agents.banks.nordea.NordeaHashMapDeserializer;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfirmPaymentsOut {

    private ConfirmationStatus confirmationStatus;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String rejectedAmount;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String rejectedCount;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String rejectedCurrency;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String type;

    public ConfirmationStatus getConfirmationStatus() {
        return confirmationStatus;
    }

    public void setConfirmationStatus(ConfirmationStatus confirmationStatus) {
        this.confirmationStatus = confirmationStatus;
    }

    public String getRejectedAmount() {
        return rejectedAmount;
    }

    public void setRejectedAmount(String rejectedAmount) {
        this.rejectedAmount = rejectedAmount;
    }

    public String getRejectedCount() {
        return rejectedCount;
    }

    public void setRejectedCount(String rejectedCount) {
        this.rejectedCount = rejectedCount;
    }

    public String getRejectedCurrency() {
        return rejectedCurrency;
    }

    public void setRejectedCurrency(String rejectedCurrency) {
        this.rejectedCurrency = rejectedCurrency;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
