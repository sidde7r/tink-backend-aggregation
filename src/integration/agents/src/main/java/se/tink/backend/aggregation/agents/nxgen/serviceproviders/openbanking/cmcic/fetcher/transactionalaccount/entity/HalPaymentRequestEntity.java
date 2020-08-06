package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class HalPaymentRequestEntity {
    @JsonProperty("paymentRequest")
    private PaymentResponseEntity paymentRequest = null;

    public PaymentResponseEntity getPaymentRequest() {
        return paymentRequest;
    }

    public void setPaymentRequest(PaymentResponseEntity paymentRequest) {
        this.paymentRequest = paymentRequest;
    }
}
