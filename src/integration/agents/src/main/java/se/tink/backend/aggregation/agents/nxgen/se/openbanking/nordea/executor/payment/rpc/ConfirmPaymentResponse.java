package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.entities.GroupHeaderEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.executor.payment.entities.PaymentResponseEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConfirmPaymentResponse {
    @JsonProperty("group_header")
    private GroupHeaderEntity groupHeaderEntity;

    private PaymentResponseEntity response;

    public PaymentResponseEntity getPaymentResponse() {
        return response;
    }
}
