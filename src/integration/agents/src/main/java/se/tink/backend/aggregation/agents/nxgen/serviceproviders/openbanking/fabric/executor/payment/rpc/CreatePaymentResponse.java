package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.executor.payment.entities.LinksEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.executor.payment.enums.FabricPaymentStatus;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
public class CreatePaymentResponse {

    private String transactionStatus;
    private String paymentId;

    private String psuMessage;
    private String scaStatus;

    @JsonProperty("_links")
    private LinksEntity links;

    private String fundsAvailable;

    @JsonIgnore
    public PaymentResponse toTinkPaymentResponse(PaymentType paymentType) {
        return toTinkPaymentResponse(paymentId, paymentType);
    }

    @JsonIgnore
    public PaymentResponse toTinkPaymentResponse(String paymentId, PaymentType paymentType) {
        Payment tinkPayment =
                new Payment.Builder()
                        .withStatus(
                                FabricPaymentStatus.mapToTinkPaymentStatus(
                                        FabricPaymentStatus.fromString(transactionStatus)))
                        .withUniqueId(paymentId)
                        .withType(paymentType)
                        .build();

        return new PaymentResponse(tinkPayment);
    }

    public String getPaymentId() {
        return paymentId;
    }

    public LinksEntity getLinks() {
        return links;
    }

    public String getTransactionStatus() {
        return transactionStatus;
    }

    public String getPsuMessage() {
        return psuMessage;
    }

    public String getScaStatus() {
        return scaStatus;
    }
}
