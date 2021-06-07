package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.entities;

import lombok.Getter;
import lombok.Setter;
import se.tink.backend.aggregation.agents.exceptions.payment.CreditorValidationException;
import se.tink.backend.aggregation.agents.exceptions.payment.DateValidationException;
import se.tink.backend.aggregation.agents.exceptions.payment.DebtorValidationException;
import se.tink.backend.aggregation.agents.exceptions.payment.InsufficientFundsException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationCancelledByUserException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationTimeOutException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentValidationException;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException.EndUserMessage;
import se.tink.libraries.signableoperation.enums.InternalStatus;

@Getter
@Setter
public class StatusReasonInformationEntity {
    private String value;

    StatusReasonInformationEntity(String value) {
        this.value = value;
    }

    public void mapToError() throws PaymentException {
        switch (value) {
            case "AC01":
            case "AC04":
            case "AC06":
            case "AG01":
                throw new PaymentValidationException(
                        EndUserMessage.INVALID_SOURCE.getKey().get(),
                        InternalStatus.INVALID_SOURCE_ACCOUNT);
            case "AM18":
                throw new PaymentRejectedException(
                        "The number of transactions exceeds the acceptance limit.",
                        InternalStatus.TRANSFER_LIMIT_REACHED);
            case "CH03":
                throw DateValidationException.paymentDateTooFarException();
            case "CUST":
                throw new InsufficientFundsException();
            case "DS02":
                throw new PaymentAuthorizationCancelledByUserException();
            case "FF01":
                throw new PaymentValidationException(
                        InternalStatus.PAYMENT_VALIDATION_FAILED_NO_DESCRIPTION);
            case "FRAD":
                throw PaymentRejectedException.fraudulentPaymentException();
            case "MS03":
            case "RR04":
                throw new PaymentRejectedException();
            case "NOAS":
                throw new PaymentAuthorizationTimeOutException();
            case "RR01":
                throw DebtorValidationException.invalidAccount();
            case "RR03":
                throw CreditorValidationException.invalidAccount();
            case "RR12":
                throw new PaymentValidationException(
                        "Payment type is invalid", InternalStatus.INVALID_PAYMENT_TYPE);
            default:
                throw new PaymentException(InternalStatus.BANK_ERROR_CODE_NOT_HANDLED_YET);
        }
    }
}
