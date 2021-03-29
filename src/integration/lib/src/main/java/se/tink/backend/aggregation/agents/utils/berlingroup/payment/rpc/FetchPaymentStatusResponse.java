package se.tink.backend.aggregation.agents.utils.berlingroup.payment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.enums.AspspPaymentStatus;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
public class FetchPaymentStatusResponse {
    private String transactionStatus;

    @JsonIgnore
    public PaymentResponse toTinkPayment(Payment payment) {
        payment.setStatus(AspspPaymentStatus.fromString(transactionStatus).getPaymentStatus());

        return new PaymentResponse(payment);
    }
}
