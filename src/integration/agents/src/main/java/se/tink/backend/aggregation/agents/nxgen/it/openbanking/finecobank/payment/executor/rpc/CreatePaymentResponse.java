package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.executor.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.executor.entities.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
public class CreatePaymentResponse {
    private String paymentId;
    private String transactionStatus;

    @JsonProperty("_links")
    private LinksEntity links;

    @JsonIgnore
    public String getScaRedirectLink() {
        return Optional.ofNullable(links)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_SIGNING_LINK))
                .getScaRedirectLink();
    }

    @JsonIgnore
    public PaymentResponse toTinkPaymentResponse(PaymentRequest paymentRequest) {

        Payment payment =
                new Payment.Builder()
                        .withExactCurrencyAmount(
                                paymentRequest.getPayment().getExactCurrencyAmount())
                        .withCreditor(paymentRequest.getPayment().getCreditor())
                        .withDebtor(paymentRequest.getPayment().getDebtor())
                        .withCurrency(paymentRequest.getPayment().getCurrency())
                        .withUniqueId(paymentId)
                        .withStatus(PaymentStatus.PENDING)
                        .withRemittanceInformation(
                                paymentRequest.getPayment().getRemittanceInformation())
                        .build();

        return new PaymentResponse(payment);
    }

    @JsonIgnore
    public String getPaymentId() {
        return paymentId;
    }
}
