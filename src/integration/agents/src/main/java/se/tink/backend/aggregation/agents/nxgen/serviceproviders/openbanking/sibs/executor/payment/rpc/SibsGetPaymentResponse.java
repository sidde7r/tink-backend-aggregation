package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.rpc;

import java.time.LocalDate;
import java.util.List;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.entities.SibsAccountReferenceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.entities.SibsAddressEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.entities.SibsAmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.entities.SibsTppMessage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.entities.dictionary.SibsTransactionStatus;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.storage.Storage;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
public class SibsGetPaymentResponse {

    private SibsTransactionStatus transactionStatus;
    private String paymentId;
    private SibsAccountReferenceEntity debtorAccount;
    private SibsAmountEntity instructedAmount;
    private SibsAccountReferenceEntity creditorAccount;
    private String creditorAgent;
    private String creditorName;
    private SibsAddressEntity creditorAddress;
    private SibsAmountEntity transactionFees;
    private boolean transactionFeeIndicator;
    private String creditorClearingCode;
    private LocalDate requestedExecutionDate;
    private List<SibsTppMessage> tppMessages;

    public SibsTransactionStatus getTransactionStatus() {
        return transactionStatus;
    }

    public void setTransactionStatus(SibsTransactionStatus transactionStatus) {
        this.transactionStatus = transactionStatus;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public SibsAccountReferenceEntity getDebtorAccount() {
        return debtorAccount;
    }

    public void setDebtorAccount(SibsAccountReferenceEntity debtorAccount) {
        this.debtorAccount = debtorAccount;
    }

    public SibsAmountEntity getInstructedAmount() {
        return instructedAmount;
    }

    public void setInstructedAmount(SibsAmountEntity instructedAmount) {
        this.instructedAmount = instructedAmount;
    }

    public SibsAccountReferenceEntity getCreditorAccount() {
        return creditorAccount;
    }

    public void setCreditorAccount(SibsAccountReferenceEntity creditorAccount) {
        this.creditorAccount = creditorAccount;
    }

    public String getCreditorAgent() {
        return creditorAgent;
    }

    public void setCreditorAgent(String creditorAgent) {
        this.creditorAgent = creditorAgent;
    }

    public String getCreditorName() {
        return creditorName;
    }

    public void setCreditorName(String creditorName) {
        this.creditorName = creditorName;
    }

    public SibsAddressEntity getCreditorAddress() {
        return creditorAddress;
    }

    public void setCreditorAddress(SibsAddressEntity creditorAddress) {
        this.creditorAddress = creditorAddress;
    }

    public SibsAmountEntity getTransactionFees() {
        return transactionFees;
    }

    public void setTransactionFees(SibsAmountEntity transactionFees) {
        this.transactionFees = transactionFees;
    }

    public boolean isTransactionFeeIndicator() {
        return transactionFeeIndicator;
    }

    public void setTransactionFeeIndicator(boolean transactionFeeIndicator) {
        this.transactionFeeIndicator = transactionFeeIndicator;
    }

    public String getCreditorClearingCode() {
        return creditorClearingCode;
    }

    public void setCreditorClearingCode(String creditorClearingCode) {
        this.creditorClearingCode = creditorClearingCode;
    }

    public LocalDate getRequestedExecutionDate() {
        return requestedExecutionDate;
    }

    public void setRequestedExecutionDate(LocalDate requestedExecutionDate) {
        this.requestedExecutionDate = requestedExecutionDate;
    }

    public List<SibsTppMessage> getTppMessages() {
        return tppMessages;
    }

    public void setTppMessages(List<SibsTppMessage> tppMessages) {
        this.tppMessages = tppMessages;
    }

    public PaymentResponse toTinkPaymentResponse(Storage sessionStorage) throws PaymentException {

        Payment.Builder buildingPaymentResponse =
                new Payment.Builder()
                        .withCreditor(creditorAccount.toTinkCreditor())
                        .withDebtor(debtorAccount.toTinkDebtor())
                        .withAmount(instructedAmount.toTinkAmount())
                        .withExecutionDate(requestedExecutionDate)
                        .withCurrency(instructedAmount.getCurrency())
                        .withStatus(getTransactionStatus().getTinkStatus())
                        .withUniqueId(getPaymentId());
        PaymentResponse response =
                new PaymentResponse(buildingPaymentResponse.build(), sessionStorage);
        return response;
    }
}
