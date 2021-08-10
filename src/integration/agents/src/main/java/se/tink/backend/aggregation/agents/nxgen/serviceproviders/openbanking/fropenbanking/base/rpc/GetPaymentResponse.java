package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.entities.PaymentEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.enums.BankPaymentStatus;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payment.rpc.Payment.Builder;

@JsonObject
@NoArgsConstructor
@AllArgsConstructor
public class GetPaymentResponse {

    @JsonProperty("paymentRequest")
    private PaymentEntity payment;

    public PaymentResponse toTinkPaymentResponse(String paymentId) {
        Payment tinkPayment =
                new Builder()
                        .withUniqueId(paymentId)
                        .withType(getPaymentType())
                        .withStatus(getPaymentStatus())
                        .withCurrency(payment.getAmount().getCurrency())
                        .withExactCurrencyAmount(payment.getAmount().toTinkAmount())
                        .withCreditor(payment.getCreditorAccount().toTinkCreditor())
                        .withDebtor(payment.getDebtorAccount().toTinkDebtor())
                        .build();

        return new PaymentResponse(tinkPayment);
    }

    private PaymentType getPaymentType() {
        return PaymentType.valueOf(payment.getPaymentType());
    }

    public PaymentStatus getPaymentStatus() {
        return BankPaymentStatus.fromString(payment.getPaymentStatus()).getPaymentStatus();
    }

    public String getStatusReasonInformation() {
        return payment.getStatusReasonInformation();
    }
}
