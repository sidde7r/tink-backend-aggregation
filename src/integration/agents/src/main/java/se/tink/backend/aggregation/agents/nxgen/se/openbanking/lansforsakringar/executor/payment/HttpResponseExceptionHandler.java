package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment;

import static se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants.ErrorMessages.INVALID_CREDITOR_ACCOUNT;
import static se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants.ErrorMessages.INVALID_INFO_STRUCTURED;
import static se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants.ErrorMessages.INVALID_INFO_UNSTRUCTURED;
import static se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants.ErrorMessages.INVALID_REQUESTED_EXECUTION_DATE;
import static se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants.ErrorMessages.NOT_ENOUGH_FUNDS;
import static se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants.ErrorMessages.REMITTANCE_INFO_NOT_SET_FOR_GIROS;
import static se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants.ErrorMessages.REQUESTED_DATE_CAN_NOT_BE_IN_THE_PAST;
import static se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants.ErrorMessages.SERVICE_BLOCKED;
import static se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants.ErrorMessages.STRING_AFTER_BANK_ERROR_MESSAGE;
import static se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants.ErrorMessages.STRING_BEFORE_BANK_ERROR_MESSAGE;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.exceptions.payment.CreditorValidationException;
import se.tink.backend.aggregation.agents.exceptions.payment.DateValidationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
import se.tink.backend.aggregation.agents.exceptions.payment.ReferenceValidationException;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.signableoperation.enums.InternalStatus;

@Getter
@Setter
@JsonObject
public class HttpResponseExceptionHandler {

    public static void checkForErrors(String errorMessage) throws PaymentException {

        if (isInvalidInfoStructured(errorMessage)) {
            throw new ReferenceValidationException(INVALID_INFO_STRUCTURED);
        } else if (isInvalidInfoUnstructured(errorMessage)) {
            throw new ReferenceValidationException(INVALID_INFO_UNSTRUCTURED);
        } else if (isRemittanceInfoSetForGirosPayment(errorMessage)) {
            throw new ReferenceValidationException(REMITTANCE_INFO_NOT_SET_FOR_GIROS);
        } else if (isServiceBlocked(errorMessage)) {
            throw PaymentRejectedException.bankPaymentServiceUnavailable();
        } else if (isInvalidCreditorAccount(errorMessage)) {
            throw new CreditorValidationException(
                    INVALID_CREDITOR_ACCOUNT, InternalStatus.INVALID_DESTINATION_ACCOUNT);
        } else if (isInvalidRequestedExecutionDate(errorMessage)) {
            throw new DateValidationException(REQUESTED_DATE_CAN_NOT_BE_IN_THE_PAST);
        } else if (isNotEnoughFundsToMakePayment(errorMessage)) {
            throw new DateValidationException(NOT_ENOUGH_FUNDS);
        } else {
            throw new PaymentRejectedException(formatErrorMessage(errorMessage));
        }
    }

    private static String formatErrorMessage(String errorMessage) {
        return StringUtils.substringBetween(
                errorMessage, STRING_BEFORE_BANK_ERROR_MESSAGE, STRING_AFTER_BANK_ERROR_MESSAGE);
    }

    private static boolean isInvalidInfoStructured(String errorMessage) {
        return errorMessage.contains(INVALID_INFO_STRUCTURED);
    }

    private static boolean isInvalidInfoUnstructured(String errorMessage) {
        return errorMessage.contains(INVALID_INFO_UNSTRUCTURED);
    }

    private static boolean isInvalidCreditorAccount(String errorMessage) {
        return errorMessage.contains(INVALID_CREDITOR_ACCOUNT);
    }

    private static boolean isServiceBlocked(String errorMessage) {
        return errorMessage.contains(SERVICE_BLOCKED);
    }

    private static boolean isInvalidRequestedExecutionDate(String errorMessage) {
        return errorMessage.contains(INVALID_REQUESTED_EXECUTION_DATE);
    }

    private static boolean isRemittanceInfoSetForGirosPayment(String errorMessage) {
        return errorMessage.contains(REMITTANCE_INFO_NOT_SET_FOR_GIROS);
    }

    private static boolean isNotEnoughFundsToMakePayment(String errorMessage) {
        return errorMessage.contains(NOT_ENOUGH_FUNDS);
    }
}
