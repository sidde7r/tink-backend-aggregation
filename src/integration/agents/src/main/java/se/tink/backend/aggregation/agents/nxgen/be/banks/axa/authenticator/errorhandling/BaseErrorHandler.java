package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.errorhandling;

import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.rpc.BaseResponse;

public class BaseErrorHandler extends ResponseErrorHandler {

    @Override
    void process(BaseResponse<?> response) {
        if (response.getErrorCode() != 0) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception(
                    formatErrorMsg(response.getErrorCode(), response.getErrorMessage()));
        }
    }

    private String formatErrorMsg(Integer errorCode, String errorMsg) {
        return String.format("Error response [code: %d, message: %s]", errorCode, errorMsg);
    }
}
