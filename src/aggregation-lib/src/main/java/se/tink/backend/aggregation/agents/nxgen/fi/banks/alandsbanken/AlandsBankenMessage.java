package se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken;

import java.util.Arrays;
import java.util.function.Supplier;
import se.tink.backend.aggregation.agents.banks.crosskey.CrossKeyMessage;
import se.tink.backend.aggregation.agents.exceptions.errors.AgentError;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.rpc.CrossKeyError;
import se.tink.libraries.i18n.LocalizableKey;

public enum AlandsBankenMessage implements CrossKeyError {
    EXPIRED_PASSWORD("Invalid password, please contact Ålandsbanken at: 0204 292 910.", AuthorizationError.ACCOUNT_BLOCKED),
    BLOCKED_PASSWORD("Your password is blocked, please contact Ålandsbanken at: 0204 292 910.", AuthorizationError.ACCOUNT_BLOCKED),
    BLOCKED_USER("Authorization failed, please contact Ålandsbanken at: 0204 292 910.", AuthorizationError.ACCOUNT_BLOCKED),
    USER_LOCKED("Authorization failed, please contact Ålandsbanken at: 0204 292 910.", AuthorizationError.ACCOUNT_BLOCKED),
    APPROVAL_NEEDED("You have unconfirmed agreements, please login to Ålandsbankens online bank using a browser.", AuthorizationError.ACCOUNT_BLOCKED),
    PINCODE_LIST_EXCEPTION("All disposable codes have been used, please contact Ålandsbanken at: 0204 292 910.", AuthorizationError.ACCOUNT_BLOCKED),
    NEW_PIN_CODE_TABLE_FAULT("All disposable codes have been used, please contact Ålandsbanken at: 0204 292 910.", AuthorizationError.ACCOUNT_BLOCKED);

    private final LocalizableKey userMessage;
    private final AgentError agentError;

    AlandsBankenMessage(String userMessage, AgentError agentError) {
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

    public static CrossKeyError find(String error,
            Supplier<? extends IllegalArgumentException> unexpectedFailure) {
        return Arrays.stream(values())
                .filter(value -> value.name().equals(error))
                .map(value -> (CrossKeyError) value)
                .findFirst()
                .orElse(CrossKeyMessage.find(error, unexpectedFailure));
    }
}
