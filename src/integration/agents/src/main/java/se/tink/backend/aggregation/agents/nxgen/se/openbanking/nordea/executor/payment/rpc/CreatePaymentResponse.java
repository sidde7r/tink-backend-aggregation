package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.entities.GroupHeaderEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.executor.payment.entities.PaymentResponseEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;

@JsonObject
public class CreatePaymentResponse {
    @JsonProperty("group_header")
    private GroupHeaderEntity groupHeader;

    private PaymentResponseEntity response;

    public PaymentResponseEntity getResponse() {
        return response;
    }

    public PaymentResponse toTinkPaymentResponse() {
        return response.toTinkPaymentResponse();
    }
}
