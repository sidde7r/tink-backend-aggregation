package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.enums.SparebankPaymentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.enums.SparebankPaymentType;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
public class GetPaymentResponse {
    private AmountEntity instructedAmount;
    private String creditorName;
    private String creditorAgent;
    private AccountEntity creditorAccount;
    private AccountEntity debtorAccount;

    @JsonIgnore
    public PaymentResponse toTinkPaymentResponse(
            Payment payment, SparebankPaymentType paymentType, String paymentStatus) {
        Payment tinkPayment =
                new Payment.Builder()
                        .withUniqueId(payment.getUniqueId())
                        .withStatus(
                                SparebankPaymentStatus.fromString(paymentStatus)
                                        .getTinkPaymentStatus())
                        .withType(paymentType.getTinkPaymentType())
                        .withCurrency(instructedAmount.getCurrency())
                        .withExactCurrencyAmount(instructedAmount.toTinkAmount())
                        .withCreditor(creditorAccount.toTinkCreditor())
                        .withDebtor(debtorAccount.toTinkDebtor())
                        .build();

        return new PaymentResponse(tinkPayment);
    }
}
