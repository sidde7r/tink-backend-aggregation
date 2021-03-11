package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment;

import lombok.Getter;
import lombok.Setter;
import se.tink.backend.aggregation.agents.exceptions.payment.CreditorValidationException;
import se.tink.backend.aggregation.agents.exceptions.payment.DateValidationException;
import se.tink.backend.aggregation.agents.exceptions.payment.InsufficientFundsException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
import se.tink.backend.aggregation.agents.exceptions.payment.ReferenceValidationException;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException.EndUserMessage;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.signableoperation.enums.InternalStatus;

@Getter
@Setter
@JsonObject
public class HttpResponseExceptionHandler {

    public static void checkForErrors(String errorMessage) throws PaymentException {

        if (isInvalidInfoStructured(errorMessage)) {
            throw new ReferenceValidationException(EndUserMessage.INVALID_OCR.getKey().get());
        } else if (isInvalidInfoUnstructured(errorMessage)) {
            throw new ReferenceValidationException(EndUserMessage.INVALID_MESSAGE.getKey().get());
        } else if (isRemittanceInfoSetForGirosPayment(errorMessage)) {
            throw new ReferenceValidationException(EndUserMessage.INVALID_MESSAGE.getKey().get());
        } else if (isServiceBlocked(errorMessage)) {
            throw new PaymentRejectedException();
        } else if (isInvalidCreditorAccount(errorMessage)) {
            throw new CreditorValidationException(
                    EndUserMessage.INVALID_DESTINATION.getKey().get(),
                    InternalStatus.INVALID_DESTINATION_ACCOUNT);
        } else if (isInvalidRequestedExecutionDate(errorMessage)) {
            throw new DateValidationException(DateValidationException.DEFAULT_MESSAGE);
        } else if (isNotEnoughFundsToMakePayment(errorMessage)) {
            throw new InsufficientFundsException(InsufficientFundsException.DEFAULT_MESSAGE);
        } else {
            throw new PaymentRejectedException(PaymentRejectedException.MESSAGE);
        }
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
