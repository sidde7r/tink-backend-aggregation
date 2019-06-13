package se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.executor.payment.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.executor.payment.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.executor.payment.entities.CreditorAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.executor.payment.entities.DebtorAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.executor.payment.entities.PaymentProduct;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
public class CreatePaymentResponse {
    private String paymentId;
    private String transactionStatus;
    private String templateId;
    private String debtorAccountMessage;
    private String creditorAccountMessage;
    private String requestedExecutionDate;
    private AmountEntity instructedAmount;
    private DebtorAccountEntity debtorAccount;
    private CreditorAccountEntity creditorAccount;

    public PaymentResponse toTinkPaymentResponse(String paymentProduct, PaymentType paymentType) {
        Long unscalledValue =
                Double.valueOf(Double.parseDouble(instructedAmount.getAmount()) * 100).longValue();
        Amount amount = Amount.valueOf(instructedAmount.getCurrency(), unscalledValue, 2);
        Payment.Builder buildingPaymentResponse =
                new Payment.Builder()
                        .withDebtor(debtorAccount.toTinkDebtor())
                        .withCreditor(
                                creditorAccount.toTinkCreditor(
                                        PaymentProduct.fromString(paymentProduct)))
                        .withAmount(amount)
                        .withCurrency(instructedAmount.getCurrency())
                        .withStatus(
                                PaymentStatus
                                        .PENDING) // needs to be hard coded because the transaction
                        // status is always null
                        .withType(paymentType)
                        .withUniqueId(paymentId);

        Payment tinkPayment = buildingPaymentResponse.build();
        return new PaymentResponse(tinkPayment);
    }
}
