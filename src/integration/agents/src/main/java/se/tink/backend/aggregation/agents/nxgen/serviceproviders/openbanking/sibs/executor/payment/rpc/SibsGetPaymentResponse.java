package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.rpc;

import java.time.LocalDate;
import java.util.List;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.entities.SibsAccountReferenceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.entities.SibsAddressEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.entities.SibsAmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.entities.SibsTppMessageEntity;
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
    private List<SibsTppMessageEntity> tppMessages;

    public SibsTransactionStatus getTransactionStatus() {
        return transactionStatus;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public SibsAccountReferenceEntity getDebtorAccount() {
        return debtorAccount;
    }

    public SibsAmountEntity getInstructedAmount() {
        return instructedAmount;
    }

    public SibsAccountReferenceEntity getCreditorAccount() {
        return creditorAccount;
    }

    public String getCreditorAgent() {
        return creditorAgent;
    }

    public String getCreditorName() {
        return creditorName;
    }

    public SibsAddressEntity getCreditorAddress() {
        return creditorAddress;
    }

    public SibsAmountEntity getTransactionFees() {
        return transactionFees;
    }

    public boolean isTransactionFeeIndicator() {
        return transactionFeeIndicator;
    }

    public String getCreditorClearingCode() {
        return creditorClearingCode;
    }

    public LocalDate getRequestedExecutionDate() {
        return requestedExecutionDate;
    }

    public List<SibsTppMessageEntity> getTppMessages() {
        return tppMessages;
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
        return new PaymentResponse(buildingPaymentResponse.build(), sessionStorage);
    }
}
