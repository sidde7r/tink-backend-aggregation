package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.executor.payment.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.StarlingConstants;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.executor.payment.entity.PaymentOrders;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
public class PaymentOrderPaymentsResponse {

    List<PaymentOrders> payments;

    public PaymentResponse toPaymentResponse() {
        Payment payment =
                new Payment.Builder()
                        .withUniqueId(payments.get(0).getPaymentUid())
                        .withStatus(
                                StarlingConstants.PAYMENT_STATUS_MAPPER
                                        .translate(
                                                payments.get(0)
                                                        .getPaymentStatusDetails()
                                                        .getPaymentStatus())
                                        .orElse(PaymentStatus.UNDEFINED))
                        .build();
        return new PaymentResponse(payment);
    }
}
