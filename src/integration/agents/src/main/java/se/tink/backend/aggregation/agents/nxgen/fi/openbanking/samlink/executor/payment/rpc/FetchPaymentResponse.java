package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.samlink.executor.payment.rpc;

import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.samlink.SamlinkConstants;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.samlink.executor.payment.entity.CreditorAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.samlink.executor.payment.entity.CreditorAddress;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.samlink.executor.payment.entity.DebtorAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.samlink.executor.payment.entity.InstructedAmountResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
public class FetchPaymentResponse {

    private String paymentId;
    private String transactionStatus;
    private CreditorAccountResponse creditorAccount;
    private CreditorAddress creditorAddress;
    private String creditorAgent;
    private String creditorName;
    private DebtorAccountResponse debtorAccount;
    private String endToEndIdentification;
    private InstructedAmountResponse instructedAmount;
    private String remittanceInformationUnstructured;

    public PaymentResponse toTinkPayment(boolean isSepa) {
        Payment payment =
                new Payment.Builder()
                        .withCreditor(creditorAccount.toTinkCreditor())
                        .withDebtor(debtorAccount.toTinkDebtor())
                        .withAmount(Amount.inEUR(instructedAmount.getAmount()))
                        .withCurrency(instructedAmount.getCurrency())
                        .withUniqueId(paymentId)
                        .withStatus(
                                SamlinkConstants.PAYMENT_STATUS_MAPPER
                                        .translate(transactionStatus)
                                        .orElse(PaymentStatus.UNDEFINED))
                        .withType(isSepa ? PaymentType.SEPA : PaymentType.INTERNATIONAL)
                        .build();

        return new PaymentResponse(payment);
    }

    public String getPaymentId() {
        return paymentId;
    }

    public String getTransactionStatus() {
        return transactionStatus;
    }

    public CreditorAccountResponse getCreditorAccount() {
        return creditorAccount;
    }

    public CreditorAddress getCreditorAddress() {
        return creditorAddress;
    }

    public String getCreditorAgent() {
        return creditorAgent;
    }

    public String getCreditorName() {
        return creditorName;
    }

    public DebtorAccountResponse getDebtorAccount() {
        return debtorAccount;
    }

    public String getEndToEndIdentification() {
        return endToEndIdentification;
    }

    public InstructedAmountResponse getInstructedAmount() {
        return instructedAmount;
    }

    public String getRemittanceInformationUnstructured() {
        return remittanceInformationUnstructured;
    }
}
