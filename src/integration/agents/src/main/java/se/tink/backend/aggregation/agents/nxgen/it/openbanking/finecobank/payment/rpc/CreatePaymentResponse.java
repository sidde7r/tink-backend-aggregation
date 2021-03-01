package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.entities.LinksEntity;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.enums.FinecoBankPaymentStatus;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class CreatePaymentResponse {
    private String paymentId;
    private String transactionStatus;

    @JsonProperty("_links")
    private LinksEntity links;

    @JsonIgnore
    public String getScaRedirectLink() {
        return Optional.ofNullable(links)
                .map(LinksEntity::getScaRedirect)
                .filter(x -> !x.isEmpty())
                .orElse(null);
    }

    @JsonIgnore
    public PaymentResponse toTinkPaymentResponse(PaymentRequest paymentRequest) {
        Payment payment =
                new Payment.Builder()
                        .withExactCurrencyAmount(
                                paymentRequest.getPayment().getExactCurrencyAmount())
                        .withCreditor(paymentRequest.getPayment().getCreditor())
                        .withCurrency(paymentRequest.getPayment().getCurrency())
                        .withUniqueId(paymentId)
                        .withStatus(
                                FinecoBankPaymentStatus.mapToTinkPaymentStatus(
                                        FinecoBankPaymentStatus.fromString(transactionStatus)))
                        .withRemittanceInformation(
                                paymentRequest.getPayment().getRemittanceInformation())
                        .build();

        return new PaymentResponse(payment);
    }
}
