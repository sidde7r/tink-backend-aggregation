package se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.executor.payment.entities.LinksEntity;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.executor.payment.enums.RaiffeisenPaymentStatus;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
public class CreatePaymentResponse {
    private String transactionStatus;
    private String paymentId;
    private String transactionFees;
    private String transactionFeeIndicator;
    private String scaMethods;
    private String chosenScaMethod;
    private String challengeData;
    private String psuMessage;
    private String tppMessages;

    @JsonProperty("_links")
    private LinksEntity linksEntity;

    public PaymentResponse toTinkPaymentResponse() {
        Payment.Builder buildingPaymentResponse =
                new Payment.Builder()
                        .withStatus(
                                RaiffeisenPaymentStatus.mapToTinkPaymentStatus(
                                        RaiffeisenPaymentStatus.fromString(transactionStatus)))
                        .withUniqueId(paymentId);

        Payment tinkPayment = buildingPaymentResponse.build();

        return new PaymentResponse(tinkPayment);
    }
}
