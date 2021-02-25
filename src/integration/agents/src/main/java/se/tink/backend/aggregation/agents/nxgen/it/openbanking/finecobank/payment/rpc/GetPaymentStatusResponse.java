package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GetPaymentStatusResponse {
    private String transactionStatus;

    @JsonIgnore
    public String getStatus() {
        return transactionStatus;
    }
}
