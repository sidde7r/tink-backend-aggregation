package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.executor.payment.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.executor.payment.entities.CreditorAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.executor.payment.entities.DebtorAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.executor.payment.entities.PaymentProduct;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
public class FetchPaymentResponse {
    private String transactionStatus;
    private String templateId;
    private String debtorAccountMessage;
    private String creditorAccountMessage;
    private String requestedExecutionDate;
    private AmountEntity instructedAmount;
    private DebtorAccountEntity debtorAccount;
    private CreditorAccountEntity creditorAccount;

    @JsonIgnore
    public PaymentResponse toTinkPaymentResponse(
            String paymentProduct,
            String paymentId,
            PaymentType paymentType,
            PaymentStatus status) {
        final Long unscaledValue =
                Double.valueOf(Double.parseDouble(instructedAmount.getAmount()) * 100).longValue();
        Amount amount = Amount.valueOf(instructedAmount.getCurrency(), unscaledValue, 2);

        Payment.Builder buildingPaymentResponse =
                new Payment.Builder()
                        .withCreditor(
                                creditorAccount.toTinkCreditor(
                                        PaymentProduct.fromString(paymentProduct)))
                        .withAmount(amount)
                        .withDebtor(debtorAccount.toTinkDebtor())
                        .withCurrency(instructedAmount.getCurrency())
                        .withStatus(status)
                        .withType(paymentType)
                        .withUniqueId(paymentId);

        Payment tinkPayment = buildingPaymentResponse.build();
        return new PaymentResponse(tinkPayment);
    }
}
