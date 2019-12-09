package se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.executor.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.executor.entities.PaymentRequestEntity;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.executor.enums.BnpParibasFortisPaymentStatus;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.executor.enums.BnpParibasFortisPaymentType;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payment.rpc.Payment.Builder;

@JsonObject
public class GetPaymentResponse {

    @JsonProperty("paymentRequest")
    private PaymentRequestEntity payment;

    public PaymentResponse toTinkPaymentResponse(String paymentId) {
        Payment.Builder buildingPaymentResponse =
                new Builder()
                        .withUniqueId(paymentId)
                        .withType(getPaymentType())
                        .withStatus(getPaymentStatus())
                        .withCurrency(payment.getAmount().getCurrency())
                        .withAmount(payment.getAmount().toTinkAmount())
                        .withCreditor(payment.getCreditorAccount().toTinkCreditor())
                        .withDebtor(payment.getDebtorAccount().toTinkDebtor());

        Payment tinkPayment = buildingPaymentResponse.build();

        return new PaymentResponse(tinkPayment);
    }

    private PaymentType getPaymentType() {
        return BnpParibasFortisPaymentType.fromString(payment.getPaymentType())
                .getTinkPaymentType();
    }

    public PaymentStatus getPaymentStatus() {
        return BnpParibasFortisPaymentStatus.fromString(payment.getPaymentStatus())
                .getTinkPaymentStatus();
    }
}
