package se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.executor.payment.entities.OriginAccount;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.executor.payment.entities.RemoteAccount;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonInclude(Include.NON_NULL)
public class CreatePaymentRequest {
    private Double amount;
    private String communication;

    @JsonProperty("communication_type")
    private String communicationType;

    private String currency;

    @JsonProperty("execution_date")
    private String executionDate;

    @JsonProperty("origin_account")
    private OriginAccount originAccount;

    @JsonProperty("payment_id")
    private String paymentId;

    @JsonProperty("remote_account")
    private RemoteAccount remoteAccount;

    @JsonProperty("payment_treatment_type")
    private String paymentTreatmentType;

    public CreatePaymentRequest(
            Double amount,
            String communication,
            String communicationType,
            String currency,
            String executionDate,
            OriginAccount originAccount,
            String paymentId,
            RemoteAccount remoteAccount,
            String paymentTreatmentType) {
        this.amount = amount;
        this.communication = communication;
        this.communicationType = communicationType;
        this.currency = currency;
        this.executionDate = executionDate;
        this.originAccount = originAccount;
        this.paymentId = paymentId;
        this.remoteAccount = remoteAccount;
        this.paymentTreatmentType = paymentTreatmentType;
    }
}
