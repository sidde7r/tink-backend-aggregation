package se.tink.backend.aggregation.agents.banks.nordea.v20.model.payments;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import se.tink.backend.aggregation.agents.banks.nordea.NordeaHashMapDeserializer;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BankIdConfirmationOut {
    private BankIdConfirmPayments confirmPayments;

    private String progressStatus;

    public String getProgressStatus() {
        return progressStatus;
    }

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    public void setProgressStatus(String progressStatus) {
        this.progressStatus = progressStatus;
    }

    public BankIdConfirmPayments getConfirmPayments() {
        return confirmPayments;
    }

    public void setConfirmPayments(
            BankIdConfirmPayments confirmPayments) {
        this.confirmPayments = confirmPayments;
    }
}
