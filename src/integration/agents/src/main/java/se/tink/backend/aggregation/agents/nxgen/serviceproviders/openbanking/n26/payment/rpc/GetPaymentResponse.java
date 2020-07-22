package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.payment.rpc;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.payment.entities.TransferEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.payment.enums.N26PaymentStatus;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payment.rpc.Payment.Builder;

@JsonObject
@NoArgsConstructor
@AllArgsConstructor
public class GetPaymentResponse {

    private TransferEntity transfer;

    public PaymentResponse toTinkPaymentResponse(String paymentId) {
        Payment tinkPayment = new Builder().withUniqueId(paymentId).build();

        return new PaymentResponse(tinkPayment);
    }

    public PaymentStatus getPaymentStatus() {
        return N26PaymentStatus.fromString(transfer.getStatus()).getPaymentStatus();
    }
}
