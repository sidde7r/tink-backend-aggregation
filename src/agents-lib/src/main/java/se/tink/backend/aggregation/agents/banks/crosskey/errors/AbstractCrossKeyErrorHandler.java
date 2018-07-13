package se.tink.backend.aggregation.agents.banks.crosskey.errors;

import se.tink.backend.aggregation.agents.banks.crosskey.CrossKeyMessage;
import se.tink.backend.aggregation.agents.banks.crosskey.errors.exceptions.UnexpectedErrorException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;

public abstract class AbstractCrossKeyErrorHandler implements CrossKeyErrorHandler {

    @Override
    public void handleError(String code) throws Exception {
        switch (code) {
        case "PIN_CODE_INVALID":
            throw LoginError.INCORRECT_CREDENTIALS.exception(CrossKeyMessage.PIN_CODE_INVALID.getKey());
        case "ERR_PASSWORD_MISSING":
            throw LoginError.INCORRECT_CREDENTIALS.exception(CrossKeyMessage.ERR_PASSWORD_MISSING.getKey());
        case "ERR_PASSWORD_NOT_VALID":
            throw LoginError.INCORRECT_CREDENTIALS.exception(CrossKeyMessage.ERR_PASSWORD_NOT_VALID.getKey());
        case "ERR_FINNISH_USER_NAME":
            throw LoginError.INCORRECT_CREDENTIALS.exception(CrossKeyMessage.ERR_FINNISH_USER_NAME.getKey());
        case "ERR_USER_NAME_INVALID_FORMAT":
            throw LoginError.INCORRECT_CREDENTIALS.exception(CrossKeyMessage.ERR_USER_NAME_INVALID_FORMAT.getKey());
        case "ERR_PASSWORD_INVALID_FORMAT":
            throw LoginError.INCORRECT_CREDENTIALS.exception(CrossKeyMessage.ERR_PASSWORD_INVALID_FORMAT.getKey());
        case "ERR_PASSWORD_TOKEN_LOGIN_FAILED":
            throw LoginError.CREDENTIALS_VERIFICATION_ERROR.exception(CrossKeyMessage.ERR_PASSWORD_TOKEN_LOGIN_FAILED.getKey());
        case "EXPIRED_PASSWORD":
            throw LoginError.INCORRECT_CREDENTIALS.exception(CrossKeyMessage.EXPIRED_PASSWORD.getKey());
        case "ERR_PIN_MISSING":
            throw LoginError.INCORRECT_CREDENTIALS.exception(CrossKeyMessage.ERR_PIN_MISSING.getKey());
        case "ERR_PIN_INCOMPLETE":
            throw LoginError.INCORRECT_CREDENTIALS.exception(CrossKeyMessage.ERR_PIN_INCOMPLETE.getKey());
        case "ERR_PIN_FAULT":
            throw LoginError.INCORRECT_CREDENTIALS.exception(CrossKeyMessage.ERR_PIN_FAULT.getKey());
        case "BLOCKED_PASSWORD":
            throw AuthorizationError.ACCOUNT_BLOCKED.exception(CrossKeyMessage.BLOCKED_PASSWORD.getKey());
        case "BLOCKED_USER":
            throw AuthorizationError.ACCOUNT_BLOCKED.exception(CrossKeyMessage.BLOCKED_USER.getKey());
        case "APPROVAL_NEEDED":
            throw AuthorizationError.ACCOUNT_BLOCKED.exception(CrossKeyMessage.APPROVAL_NEEDED.getKey());
        case "PINCODE_LIST_EXCEPTION":
            throw AuthorizationError.ACCOUNT_BLOCKED.exception(CrossKeyMessage.PINCODE_LIST_EXCEPTION.getKey());
        case "NEW_PIN_CODE_TABLE_FAULT":
            throw AuthorizationError.ACCOUNT_BLOCKED.exception(CrossKeyMessage.NEW_PIN_CODE_TABLE_FAULT.getKey());
        case "BANK_ID_START_FAILED":
            throw BankIdError.UNKNOWN.exception(CrossKeyMessage.BANK_ID_START_FAILED.getKey());
        case "BANK_ID_USER_CANCEL":
            throw BankIdError.CANCELLED.exception(CrossKeyMessage.BANK_ID_USER_CANCEL.getKey());
        case "BANK_ID_SERVER_ERROR":
            throw BankIdError.UNKNOWN.exception(CrossKeyMessage.BANK_ID_SERVER_ERROR.getKey());
        case "BANK_ID_ALREADY_IN_PROGRESS":
            throw BankIdError.ALREADY_IN_PROGRESS.exception(CrossKeyMessage.BANK_ID_ALREADY_IN_PROGRESS.getKey());
        case "BANK_ID_NOT_INSTALLED_TITLE":
            throw BankIdError.NO_CLIENT.exception(CrossKeyMessage.BANK_ID_NOT_INSTALLED_TITLE.getKey());
        case "BANK_ID_NOT_INSTALLED":
            throw BankIdError.NO_CLIENT.exception(CrossKeyMessage.BANK_ID_NOT_INSTALLED.getKey());
        case "BANK_ID_FAILED_TO_START_TITLE":
            throw BankIdError.NO_CLIENT.exception(CrossKeyMessage.BANK_ID_FAILED_TO_START_TITLE.getKey());
        case "BANK_ID_FAILED_TO_START":
            throw BankIdError.NO_CLIENT.exception(CrossKeyMessage.BANK_ID_FAILED_TO_START.getKey());
        case "BANK_ID_EXPIRED_TRANSACTION":
            throw BankIdError.TIMEOUT.exception(CrossKeyMessage.BANK_ID_EXPIRED_TRANSACTION.getKey());
        default:
            throw new UnexpectedErrorException(String.format("Unknown error (%s)", code));
        }
    }
}
