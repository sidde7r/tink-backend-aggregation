package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.executor.payment.entities.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
public class CreatePaymentResponse {
    @JsonProperty("_links")
    private LinksEntity links;

    private String paymentId;
    private String transactionStatus;

    public LinksEntity getLinks() {
        return links;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public PaymentResponse toTinkPayment() {
        Payment.Builder buildingPaymentResponse =
                new Payment.Builder()
                        .withUniqueId(paymentId)
                        .withStatus(
                                Xs2aDevelopersConstants.PAYMENT_STATUS_MAPPER
                                        .translate(transactionStatus)
                                        .orElse(PaymentStatus.UNDEFINED))
                        .withType(PaymentType.SEPA);
        Payment tinkPayment = buildingPaymentResponse.build();
        return new PaymentResponse(tinkPayment);
    }
}
