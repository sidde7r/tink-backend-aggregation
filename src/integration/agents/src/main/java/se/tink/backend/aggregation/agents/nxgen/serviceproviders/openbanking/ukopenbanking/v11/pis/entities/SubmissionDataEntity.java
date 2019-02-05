package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v11.pis.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.UkOpenBankingConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SubmissionDataEntity {
    @JsonProperty("PaymentSubmissionId")
    private String paymentSubmissionId;
    @JsonProperty("PaymentId")
    private String paymentId;
    @JsonProperty("Status")
    private UkOpenBankingConstants.TransactionIndividualStatus1Code status;
    @JsonProperty("CreationDateTime")
    private String creationDateTime;

    public String getPaymentSubmissionId() {
        return paymentSubmissionId;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public UkOpenBankingConstants.TransactionIndividualStatus1Code getStatus() {
        return status;
    }

    public String getCreationDateTime() {
        return creationDateTime;
    }
}
