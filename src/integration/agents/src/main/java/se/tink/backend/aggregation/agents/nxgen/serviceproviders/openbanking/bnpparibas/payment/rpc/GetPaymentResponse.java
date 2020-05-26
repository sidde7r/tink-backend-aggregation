package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.payment.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.payment.entities.PaymentEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.payment.enums.BnpParibasPaymentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.payment.enums.BnpParibasPaymentType;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payment.rpc.Payment.Builder;

@JsonObject
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
        return BnpParibasPaymentType.fromString(payment.getPaymentType()).getPaymentType();
    }

    public PaymentStatus getPaymentStatus() {
        return BnpParibasPaymentStatus.fromString(payment.getPaymentStatus()).getPaymentStatus();
    }
}
