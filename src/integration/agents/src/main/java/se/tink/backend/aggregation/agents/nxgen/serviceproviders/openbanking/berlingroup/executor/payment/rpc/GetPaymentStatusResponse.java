package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.executor.payment.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.executor.payment.enums.BerlinGroupPaymentType;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.payment.rpc.Payment;

public class GetPaymentStatusResponse extends BerlinGroupBasePaymentResponse {

    @Override
    public PaymentResponse toTinkPaymentResponse(
            Payment payment, BerlinGroupPaymentType paymentType) {
        Payment.Builder buildingPaymentResponse =
                getBuildingPaymentResponse(payment, paymentType)
                        .withUniqueId(payment.getUniqueId());

        Payment tinkPayment = buildingPaymentResponse.build();

        return new PaymentResponse(tinkPayment);
    }
}
