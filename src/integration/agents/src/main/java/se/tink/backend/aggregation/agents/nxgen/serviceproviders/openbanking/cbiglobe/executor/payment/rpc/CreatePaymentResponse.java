package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.executor.payment.entities.LinksEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.executor.payment.enums.CbiGlobePaymentStatus;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
public class CreatePaymentResponse {

    private String transactionStatus;
    private String paymentId;

    private String psuAuthenticationStatus;
    private String scaStatus;

    @JsonProperty("_links")
    private LinksEntity links;

    @JsonIgnore
    public PaymentResponse toTinkPaymentResponse(PaymentType paymentType) {
        Payment.Builder buildingPaymentResponse =
                new Payment.Builder()
                        .withStatus(
                                CbiGlobePaymentStatus.mapToTinkPaymentStatus(
                                        CbiGlobePaymentStatus.fromString(transactionStatus)))
                        .withUniqueId(paymentId)
                        .withType(paymentType);

        Payment tinkPayment = buildingPaymentResponse.build();

        return new PaymentResponse(tinkPayment);
    }

    @JsonIgnore
    public PaymentResponse toTinkPaymentResponse(String paymentId, PaymentType paymentType) {
        Payment.Builder buildingPaymentResponse =
                new Payment.Builder()
                        .withStatus(
                                CbiGlobePaymentStatus.mapToTinkPaymentStatus(
                                        CbiGlobePaymentStatus.fromString(transactionStatus)))
                        .withUniqueId(paymentId)
                        .withType(paymentType);

        Payment tinkPayment = buildingPaymentResponse.build();

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

    public String getPsuAuthenticationStatus() {
        return psuAuthenticationStatus;
    }

    public String getScaStatus() {
        return scaStatus;
    }
}
