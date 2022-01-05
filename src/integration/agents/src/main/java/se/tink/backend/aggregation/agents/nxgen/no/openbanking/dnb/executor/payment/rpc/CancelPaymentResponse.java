package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.executor.payment.enums.DnbPaymentStatus;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.executor.payment.enums.DnbPaymentType;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
@Getter
public class CancelPaymentResponse {

    @JsonProperty("_links")
    private Links links;

    private String transactionStatus;

    @JsonIgnore
    public PaymentResponse toTinkPaymentResponse(
            Payment paymentRequest, DnbPaymentType dnbPaymentType) {
        Payment.Builder buildingPaymentResponse =
                new Payment.Builder()
                        .withUniqueId(paymentRequest.getUniqueId())
                        .withPaymentServiceType(paymentRequest.getPaymentServiceType())
                        .withPaymentScheme(paymentRequest.getPaymentScheme())
                        .withStatus(
                                DnbPaymentStatus.fromString(transactionStatus)
                                        .getTinkPaymentStatus())
                        .withType(dnbPaymentType.getTinkPaymentType())
                        .withCurrency(paymentRequest.getCurrency())
                        .withExactCurrencyAmount(paymentRequest.getExactCurrencyAmount())
                        .withCreditor(paymentRequest.getCreditor())
                        .withDebtor(paymentRequest.getDebtor());

        Payment tinkPayment = buildingPaymentResponse.build();

        return new PaymentResponse(tinkPayment);
    }
}
