package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.rpc;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.entities.SibsAccountReferenceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.entities.SibsAddressEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.entities.SibsAmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.entities.SibsTppMessageEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.entities.dictionary.SibsTransactionStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.utils.SibsUtils;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
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
    private String requestedExecutionDate;
    private List<SibsTppMessageEntity> tppMessages;

    private final Logger logger = LoggerFactory.getLogger(SibsGetPaymentResponse.class);

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

    public String getRequestedExecutionDate() {
        return requestedExecutionDate;
    }

    public List<SibsTppMessageEntity> getTppMessages() {
        return tppMessages;
    }

    public PaymentResponse toTinkPaymentResponse(PaymentRequest paymentRequest)
            throws PaymentException {

        Payment.Builder buildingPaymentResponse =
                new Payment.Builder()
                        .withCreditor(creditorAccount.toTinkCreditor())
                        .withDebtor(debtorAccount != null ? debtorAccount.toTinkDebtor() : null)
                        .withExactCurrencyAmount(instructedAmount.toTinkAmount())
                        .withExecutionDate(
                                SibsUtils.convertStringToLocalDate(requestedExecutionDate))
                        .withCurrency(instructedAmount.getCurrency())
                        .withStatus(getTransactionStatus().getTinkStatus())
                        .withPaymentScheme(paymentRequest.getPayment().getPaymentScheme())
                        .withUniqueId(getPaymentId());

        logger.info("Payment execution date set to: {}", requestedExecutionDate);

        return new PaymentResponse(buildingPaymentResponse.build(), paymentRequest.getStorage());
    }
}
