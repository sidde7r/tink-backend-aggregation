package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.exceptions.payment.CreditorValidationException;
import se.tink.backend.aggregation.agents.exceptions.payment.DateValidationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
import se.tink.backend.aggregation.agents.exceptions.payment.ReferenceValidationException;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.signableoperation.enums.InternalStatus;

@Getter
@Setter
@JsonObject
public class HttpResponseExceptionHandler {

    public static void checkForErrors(String errorMessage) throws PaymentException {

        if (isInvalidInfoStructured(errorMessage)) {
            throw new ReferenceValidationException(ErrorMessages.INVALID_INFO_STRUCTURED);
        } else if (isInvalidInfoUnstructured(errorMessage)) {
            throw new ReferenceValidationException(ErrorMessages.INVALID_INFO_UNSTRUCTURED);
        } else if (isRemittanceInfoSetForGirosPayment(errorMessage)) {
            throw new ReferenceValidationException(ErrorMessages.REMITTANCE_INFO_NOT_SET_FOR_GIROS);
        } else if (isServiceBlocked(errorMessage)) {
            throw PaymentRejectedException.bankPaymentServiceUnavailable();
        } else if (isInvalidCreditorAccount(errorMessage)) {
            throw new CreditorValidationException(
                    ErrorMessages.INVALID_CREDITOR_ACCOUNT,
                    InternalStatus.INVALID_DESTINATION_ACCOUNT);
        } else if (isInvalidRequestedExecutionDate(errorMessage)) {
            throw new DateValidationException(ErrorMessages.REQUESTED_DATE_CAN_NOT_BE_IN_THE_PAST);
        } else if (isNotEnoughFundsToMakePayment(errorMessage)) {
            throw new DateValidationException(ErrorMessages.NOT_ENOUGH_FUNDS);
        } else {
            throw new PaymentRejectedException(formatErrorMessage(errorMessage));
        }
    }

    private static String formatErrorMessage(String errorMessage) {
        return StringUtils.substringBetween(
                errorMessage,
                ErrorMessages.STRING_BEFORE_BANK_ERROR_MESSAGE,
                ErrorMessages.STRING_AFTER_BANK_ERROR_MESSAGE);
    }

    private static boolean isInvalidInfoStructured(String errorMessage) {
        return errorMessage.contains(ErrorMessages.INVALID_INFO_STRUCTURED);
    }

    private static boolean isInvalidInfoUnstructured(String errorMessage) {
        return errorMessage.contains(ErrorMessages.INVALID_INFO_UNSTRUCTURED);
    }

    private static boolean isInvalidCreditorAccount(String errorMessage) {
        return errorMessage.contains(ErrorMessages.INVALID_CREDITOR_ACCOUNT);
    }

    private static boolean isServiceBlocked(String errorMessage) {
        return errorMessage.contains(ErrorMessages.SERVICE_BLOCKED);
    }

    private static boolean isInvalidRequestedExecutionDate(String errorMessage) {
        return errorMessage.contains(ErrorMessages.INVALID_REQUESTED_EXECUTION_DATE);
    }

    private static boolean isRemittanceInfoSetForGirosPayment(String errorMessage) {
        return errorMessage.contains(ErrorMessages.REMITTANCE_INFO_NOT_SET_FOR_GIROS);
    }

    private static boolean isNotEnoughFundsToMakePayment(String errorMessage) {
        return errorMessage.contains(ErrorMessages.NOT_ENOUGH_FUNDS);
    }
}
