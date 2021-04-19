package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.apiclient.error;

import se.tink.backend.aggregation.agents.exceptions.payment.CreditorValidationException;
import se.tink.backend.aggregation.agents.exceptions.payment.DebtorValidationException;
import se.tink.backend.aggregation.agents.exceptions.payment.InsufficientFundsException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.apiclient.dto.ErrorResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.signableoperation.enums.InternalStatus;

public class DemobankErrorHandler {

    public void remapException(HttpResponseException e) throws PaymentException {
        ErrorResponse errorResponse = e.getResponse().getBody(ErrorResponse.class);
        if (errorResponse.hasInsufficientFunds()) {
            throw new InsufficientFundsException(
                    errorResponse.getErrorMessage(ErrorCodes.INSUFFICIENT_FUNDS),
                    InternalStatus.INSUFFICIENT_FUNDS);
        }

        if (errorResponse.isInvalidDebtor()) {
            throw new DebtorValidationException(
                    errorResponse.getErrorMessage(ErrorCodes.INVALID_DEBTOR_ACCOUNT),
                    InternalStatus.INVALID_DESTINATION_ACCOUNT);
        }

        if (errorResponse.isInvalidCreditor()) {
            throw new CreditorValidationException(
                    errorResponse.getErrorMessage(ErrorCodes.INVALID_ACCOUNT),
                    InternalStatus.INVALID_SOURCE_ACCOUNT);
        }

        if (errorResponse.isBadRequest()) {
            throw new PaymentRejectedException(
                    errorResponse.getErrorMessage(ErrorCodes.FORMAT_ERROR),
                    InternalStatus.BANK_ERROR_CODE_NOT_HANDLED_YET);
        }

        throw new PaymentException(
                errorResponse.getErrorText(), InternalStatus.BANK_ERROR_CODE_NOT_HANDLED_YET);
    }
}
