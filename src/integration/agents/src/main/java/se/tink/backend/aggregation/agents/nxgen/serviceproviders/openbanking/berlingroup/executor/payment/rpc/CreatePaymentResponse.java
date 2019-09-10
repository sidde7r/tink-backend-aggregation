package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.executor.payment.entity.PaymentResponseLink;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.executor.payment.enums.BerlinGroupPaymentType;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
public class CreatePaymentResponse extends BerlinGroupBasePaymentResponse {
    private String paymentId;

    @JsonProperty("_links")
    private PaymentResponseLink links;

    @Override
    public PaymentResponse toTinkPaymentResponse(
            Payment payment, BerlinGroupPaymentType paymentType) {
        Payment.Builder buildingPaymentResponse =
                getBuildingPaymentResponse(payment, paymentType).withUniqueId(paymentId);

        Payment tinkPayment = buildingPaymentResponse.build();

        return new PaymentResponse(tinkPayment);
    }

    public String getPaymentId() {
        return paymentId;
    }

    public PaymentResponseLink getLinks() {
        return links;
    }
}
