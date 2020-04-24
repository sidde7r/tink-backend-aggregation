package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.payments.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.payments.entities.CreditorAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.payments.entities.DebtorAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.payments.entities.InstructedAmountEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.payments.enums.DkbPaymentStatus;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
public class FetchPaymentResponse {
    private DebtorAccountEntity debtorAccount;
    private InstructedAmountEntity instructedAmount;
    private CreditorAccountEntity creditorAccount;
    private String creditorName;
    private String transactionStatus;

    @JsonIgnore
    public PaymentResponse toTinkPaymentResponse() {
        long unscaledValue = Double.valueOf(instructedAmount.getAmount() * 100).longValue();
        Payment.Builder buildingPaymentResponse =
                new Payment.Builder()
                        .withCreditor(creditorAccount.toTinkCreditor())
                        .withDebtor(debtorAccount.toTinkDebtor())
                        .withExactCurrencyAmount(
                                ExactCurrencyAmount.of(
                                        BigDecimal.valueOf(unscaledValue, 2),
                                        instructedAmount.getCurrency()))
                        .withExecutionDate(null)
                        .withCurrency(instructedAmount.getCurrency())
                        .withStatus(
                                DkbPaymentStatus.mapToTinkPaymentStatus(
                                        DkbPaymentStatus.fromString(transactionStatus)));

        Payment tinkPayment = buildingPaymentResponse.build();
        return new PaymentResponse(tinkPayment);
    }
}
