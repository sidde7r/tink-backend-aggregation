package se.tink.backend.aggregation.agents.banks.se.alandsbanken;

import se.tink.backend.aggregation.agents.banks.crosskey.errors.AbstractCrossKeyErrorHandler;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;

public class AlandsBankenErrorHandler extends AbstractCrossKeyErrorHandler {

    @Override
    public void handleError(String errorCode) throws Exception {
        switch (errorCode) {
            case "EXPIRED_PASSWORD":
                throw AuthorizationError.ACCOUNT_BLOCKED.exception(
                        AlandsBankenMessage.EXPIRED_PASSWORD.getKey());
            case "BLOCKED_PASSWORD":
                throw AuthorizationError.ACCOUNT_BLOCKED.exception(
                        AlandsBankenMessage.BLOCKED_PASSWORD.getKey());
            case "BLOCKED_USER":
                throw AuthorizationError.ACCOUNT_BLOCKED.exception(
                        AlandsBankenMessage.BLOCKED_USER.getKey());
            case "APPROVAL_NEEDED":
                throw AuthorizationError.ACCOUNT_BLOCKED.exception(
                        AlandsBankenMessage.APPROVAL_NEEDED.getKey());
            case "PINCODE_LIST_EXCEPTION":
                throw AuthorizationError.ACCOUNT_BLOCKED.exception(
                        AlandsBankenMessage.PINCODE_LIST_EXCEPTION.getKey());
            case "NEW_PIN_CODE_TABLE_FAULT":
                throw AuthorizationError.ACCOUNT_BLOCKED.exception(
                        AlandsBankenMessage.NEW_PIN_CODE_TABLE_FAULT.getKey());
            default:
                super.handleError(errorCode);
        }
    }
}
