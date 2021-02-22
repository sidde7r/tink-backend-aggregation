package se.tink.backend.aggregation.agents.nxgen.at.openbanking.erstebank.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDate;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.erstebank.ErstebankConstants;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.erstebank.executor.payment.entity.CreditorAccountRequest;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.erstebank.executor.payment.entity.DebtorAccountRequest;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
public class CreatePaymentResponse {

    private String transactionStatus;
    private String paymentId;
    private double transactionFees;
    private boolean transactionFeeIndicator;

    @JsonIgnore
    public PaymentResponse toTinkPaymentResponse(
            CreditorAccountRequest creditor,
            DebtorAccountRequest debtor,
            ExactCurrencyAmount amount,
            LocalDate executionDate) {
        Payment payment =
                new Payment.Builder()
                        .withCreditor(creditor.toTinkCreditor())
                        .withDebtor(debtor.toTinkDebtor())
                        .withExactCurrencyAmount(amount)
                        .withExecutionDate(executionDate)
                        .withCurrency(amount.getCurrencyCode())
                        .withUniqueId(paymentId)
                        .withStatus(
                                ErstebankConstants.PAYMENT_STATUS_MAPPER
                                        .translate(transactionStatus)
                                        .orElse(PaymentStatus.UNDEFINED))
                        .withType(PaymentType.UNDEFINED)
                        .build();

        return new PaymentResponse(payment);
    }
}
