package se.tink.backend.aggregation.agents.utils.berlingroup.payment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.PaymentStatusMapper;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
@AllArgsConstructor
@NoArgsConstructor
public class FetchPaymentStatusResponse {
    private String transactionStatus;

    @JsonIgnore
    public PaymentResponse toTinkPayment(Payment payment, PaymentStatusMapper paymentStatusMapper) {
        payment.setStatus(
                paymentStatusMapper.toTinkPaymentStatus(
                        transactionStatus, payment.getPaymentServiceType()));
        return new PaymentResponse(payment);
    }
}
