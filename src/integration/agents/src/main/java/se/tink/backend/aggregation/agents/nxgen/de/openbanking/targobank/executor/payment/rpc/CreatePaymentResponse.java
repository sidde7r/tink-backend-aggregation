package se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.executor.payment.entities.LinksEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.executor.payment.enums.TargobankPaymentStatus;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
public class CreatePaymentResponse {
    private String transactionStatus;
    private String paymentId;

    @JsonProperty("_links")
    private LinksEntity links;

    @JsonIgnore
    public PaymentResponse toTinkPaymentResponse() {
        Payment.Builder buildingPaymentResponse =
                new Payment.Builder()
                        .withStatus(
                                TargobankPaymentStatus.mapToTinkPaymentStatus(
                                        TargobankPaymentStatus.fromString(transactionStatus)))
                        .withUniqueId(paymentId);

        Payment tinkPayment = buildingPaymentResponse.build();

        return new PaymentResponse(tinkPayment);
    }
}
