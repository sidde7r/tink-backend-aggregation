package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.utils;

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
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.FrOpenBankingConstants.ErrorCodes;
import se.tink.libraries.signableoperation.enums.InternalStatus;

public class FrOpenBankingErrorMapper {

    public static PaymentException mapToError(String value) {
        switch (value) {
            case ErrorCodes.INCORRECT_ACCOUNT_NUMBER:
            case ErrorCodes.CLOSED_ACCOUNT_NUMBER:
            case ErrorCodes.BLOCKED_ACCOUNT:
            case ErrorCodes.TRANSACTION_FORBIDDEN:
                return new PaymentValidationException(
                        EndUserMessage.INVALID_SOURCE.getKey().get(),
                        InternalStatus.INVALID_SOURCE_ACCOUNT);
            case ErrorCodes.INVALID_NUMBER_OF_TRANSACTIONS:
                return new PaymentRejectedException(
                        "The number of transactions exceeds the acceptance limit.",
                        InternalStatus.TRANSFER_LIMIT_REACHED);
            case ErrorCodes.REQUESTED_EXECUTION_DATE_OR_REQUESTED_COLLECTION_DATE_TOO_FAR_IN_FUTURE:
                return DateValidationException.paymentDateTooFarException();
            case ErrorCodes.INSUFFICIENT_FUNDS:
                return new InsufficientFundsException();
            case ErrorCodes.ORDER_CANCELLED:
                return new PaymentAuthorizationCancelledByUserException();
            case ErrorCodes.INVALID_FILE_FORMAT:
                return new PaymentValidationException(
                        InternalStatus.PAYMENT_VALIDATION_FAILED_NO_DESCRIPTION);
            case ErrorCodes.FRAUDULENT_ORIGINATED:
                return PaymentRejectedException.fraudulentPaymentException();
            case ErrorCodes.NOT_SPECIFIED_REASON_AGENT_GENERATED:
            case ErrorCodes.REGULATORY_REASON:
                return new PaymentRejectedException();
            case ErrorCodes.NO_ANSWER_FROM_CUSTOMER:
                return new PaymentAuthorizationTimeOutException();
            case ErrorCodes.MISSING_DEBTOR_ACCOUNT_OR_IDENTIFICATION:
                return DebtorValidationException.invalidAccount();
            case ErrorCodes.MISSING_CREDITOR_NAME_OR_ADDRESS:
                return CreditorValidationException.invalidAccount();
            case ErrorCodes.INVALID_PARTY_ID:
                return new PaymentValidationException(
                        "Payment type is invalid", InternalStatus.INVALID_PAYMENT_TYPE);
            default:
                return new PaymentException(InternalStatus.BANK_ERROR_CODE_NOT_HANDLED_YET);
        }
    }
}
