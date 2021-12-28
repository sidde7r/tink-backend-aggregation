package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.executor.payment.enums.DnbPaymentStatus;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.executor.payment.enums.DnbPaymentType;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payments.common.model.PaymentScheme;
import se.tink.libraries.transfer.rpc.PaymentServiceType;

@JsonObject
@Getter
public class CreatePaymentResponse {
    private String transactionStatus;

    @JsonProperty("paymentId")
    private String paymentId;

    @JsonProperty("_links")
    private Links links;

    private String psuMessage;

    @JsonIgnore
    public PaymentResponse toTinkPaymentResponse(
            CreatePaymentRequest createPaymentRequest,
            DnbPaymentType dnbPaymentType,
            PaymentServiceType paymentServiceType,
            PaymentScheme paymentScheme) {
        Payment.Builder buildingPaymentResponse =
                new Payment.Builder()
                        .withUniqueId(paymentId)
                        .withPaymentServiceType(paymentServiceType)
                        .withPaymentScheme(paymentScheme)
                        .withStatus(
                                DnbPaymentStatus.fromString(transactionStatus)
                                        .getTinkPaymentStatus())
                        .withType(dnbPaymentType.getTinkPaymentType())
                        .withCurrency(createPaymentRequest.getAmount().getCurrency())
                        .withExactCurrencyAmount(createPaymentRequest.getAmount().toTinkAmount())
                        .withCreditor(createPaymentRequest.getCreditor().toTinkCreditor())
                        .withDebtor(createPaymentRequest.getDebtor().toTinkDebtor());

        Payment tinkPayment = buildingPaymentResponse.build();

        return new PaymentResponse(tinkPayment);
    }
}
