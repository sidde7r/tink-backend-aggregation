package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.entities.CreditorAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.entities.DebtorAccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
public class GetPaymentResponse {
    private AmountEntity instructedAmount;
    private String creditorName;
    private CreditorAccountEntity creditorAccount;
    private DebtorAccountEntity debtorAccount;

    public PaymentResponse toTinkPaymentResponse(PaymentRequest paymentRequest) {
        Payment.Builder buildingPaymentResponse =
                new Payment.Builder()
                        .withCreditor(creditorAccount.toTinkCreditor())
                        .withDebtor(debtorAccount.toTinkDebtor())
                        .withAmount(instructedAmount.toTinkAmount())
                        .withCurrency(instructedAmount.getCurrency())
                        .withStatus(PaymentStatus.PENDING)
                        .withType(paymentRequest.getPayment().getType())
                        .withUniqueId(paymentRequest.getPayment().getUniqueId());

        Payment tinkPayment = buildingPaymentResponse.build();

        return new PaymentResponse(tinkPayment);
    }
}
