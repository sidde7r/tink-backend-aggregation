package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.executor.payment.entity.PayeeEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.executor.payment.entity.PayerEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.executor.payment.enums.OpBankPaymentStatus;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
@Getter
@NoArgsConstructor
public class CreatePaymentResponse {

    private String authorizationId;
    private String amountEUR;
    private int count;
    private PayeeEntity payee;
    private PayerEntity payer;
    private String message;
    private String reference;
    private String ultimatePayee;
    private String originalPayer;
    private String paymentId;
    private String paymentType;
    private String detailsOfCharges;
    private String paymentOrder;
    private String status;

    @JsonIgnore
    public PaymentResponse toTinkPayment(PaymentRequest paymentRequest) {
        Payment.Builder builder = new Payment.Builder();

        PaymentStatus paymentStatus = getTinkStatus();

        builder.withCurrency(paymentRequest.getPayment().getCurrency())
                .withStatus(paymentStatus)
                .withType(PaymentType.SEPA)
                .withExactCurrencyAmount(paymentRequest.getPayment().getExactCurrencyAmount())
                .withUniqueId(paymentId)
                .withDebtor(paymentRequest.getPayment().getDebtor())
                .withCreditor(paymentRequest.getPayment().getCreditor());
        return new PaymentResponse(builder.build(), paymentRequest.getStorage());
    }

    @JsonIgnore
    public PaymentStatus getTinkStatus() {
        return OpBankPaymentStatus.fromString(status).getPaymentStatus();
    }
}
