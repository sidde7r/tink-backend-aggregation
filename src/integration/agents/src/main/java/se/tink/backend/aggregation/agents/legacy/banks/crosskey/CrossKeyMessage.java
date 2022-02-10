package se.tink.backend.aggregation.agents.banks.crosskey;

import java.util.Arrays;
import java.util.function.Supplier;
import se.tink.backend.aggregation.agents.exceptions.agent.AgentError;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.libraries.i18n_aggregation.LocalizableKey;

public enum CrossKeyMessage implements CrossKeyError {
    PIN_CODE_INVALID("Invalid password.", LoginError.INCORRECT_CREDENTIALS),
    ERR_PASSWORD_MISSING("The password must be provided.", LoginError.INCORRECT_CREDENTIALS),
    ERR_PASSWORD_NOT_VALID("Invalid password.", LoginError.INCORRECT_CREDENTIALS),
    EXPIRED_PASSWORD(
            "Invalid password, please contact Ålandsbanken at: 0771-415 415.",
            LoginError.INCORRECT_CREDENTIALS),
    BLOCKED_PASSWORD(
            "Your password is blocked, please contact Ålandsbanken at: 0771-415 415.",
            AuthorizationError.ACCOUNT_BLOCKED),
    BLOCKED_USER(
            "Authorization failed, please contact Ålandsbanken at: 0771-415 415.",
            AuthorizationError.ACCOUNT_BLOCKED),
    APPROVAL_NEEDED(
            "You have unconfirmed agreements, please login to Ålandsbankens online bank using a browser.",
            AuthorizationError.ACCOUNT_BLOCKED),
    ERR_FINNISH_USER_NAME("Wrong user id or password.", LoginError.INCORRECT_CREDENTIALS),
    ERR_USER_NAME_INVALID_FORMAT("Invalid user id.", LoginError.INCORRECT_CREDENTIALS),
    ERR_PASSWORD_INVALID_FORMAT("Invalid password.", LoginError.INCORRECT_CREDENTIALS),
    ERR_PASSWORD_TOKEN_LOGIN_FAILED("Invalid device.", LoginError.CREDENTIALS_VERIFICATION_ERROR),
    PINCODE_LIST_EXCEPTION(
            "All disposable codes have been used, please contact Ålandsbanken at: 0771-415 415.",
            AuthorizationError.ACCOUNT_BLOCKED),
    NEW_PIN_CODE_TABLE_FAULT(
            "All disposable codes have been used, please contact Ålandsbanken at: 0771-415 415.",
            AuthorizationError.ACCOUNT_BLOCKED),
    ERR_PIN_MISSING("One time code must be provided.", LoginError.INCORRECT_CREDENTIALS),
    ERR_PIN_INCOMPLETE(
            "The one time code was not complete, please try again.",
            LoginError.INCORRECT_CREDENTIALS),
    ERR_PIN_FAULT("Invalid one time code, please try again.", LoginError.INCORRECT_CREDENTIALS),
    BANK_ID_START_FAILED("Bank id authentication failed.", BankIdError.UNKNOWN),
    BANK_ID_USER_CANCEL("Bank id authentication canceled.", BankIdError.CANCELLED),
    BANK_ID_SERVER_ERROR(
            "Bank id can't be accessed at the moment, please try again later.",
            BankIdError.UNKNOWN),
    BANK_ID_ALREADY_IN_PROGRESS(
            "Bank id are already in progress.", BankIdError.ALREADY_IN_PROGRESS),
    BANK_ID_NOT_INSTALLED_TITLE(
            "Encountered a problem with the BankID app, please make sure you have the latest "
                    + "version of BankID installed.",
            BankIdError.NO_CLIENT),
    BANK_ID_NOT_INSTALLED(
            "Encountered a problem with the BankID app, please make sure you have the latest version of BankID installed.",
            BankIdError.NO_CLIENT),
    BANK_ID_FAILED_TO_START_TITLE(
            "Encountered a problem while opening the BankID app.", BankIdError.NO_CLIENT),
    BANK_ID_FAILED_TO_START(
            "Encountered a problem while opening the BankID app.", BankIdError.NO_CLIENT),
    BANK_ID_EXPIRED_TRANSACTION(
            "The time limit for authenticating with BankID expired.", BankIdError.TIMEOUT);

    private LocalizableKey userMessage;
    private final AgentError agentError;

    CrossKeyMessage(String userMessage, AgentError agentError) {
        this.userMessage = new LocalizableKey(userMessage);
        this.agentError = agentError;
    }

    @Override
    public LocalizableKey getKey() {
        return userMessage;
    }

    @Override
    public AgentError getAgentError() {
        return agentError;
    }

    public static CrossKeyMessage find(
            String error, Supplier<? extends IllegalArgumentException> unexpectedFailure) {
        return Arrays.stream(values())
                .filter(value -> value.name().equals(error))
                .findFirst()
                .orElseThrow(unexpectedFailure);
    }
}
