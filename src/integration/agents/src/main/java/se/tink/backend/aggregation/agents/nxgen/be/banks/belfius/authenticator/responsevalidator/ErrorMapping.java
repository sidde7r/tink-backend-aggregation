package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.responsevalidator;

import java.util.function.Supplier;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AccountBlockedError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AgentBankApiError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.DeviceRegistrationError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.InvalidCredentialsError;

public enum ErrorMapping {
    WRONG_CREDENTIALS_CODE("GE9KT60O/90AC/1307", InvalidCredentialsError::new),
    UNKNOWN_CARD("GE9KT082/1502/000000", InvalidCredentialsError::new),
    SECRET_CODE_UNSET("GE9KT60O/90AA/1361", DeviceRegistrationError::new),
    CARD_BLOCKED("GE9KT60O/90AA/1325", AccountBlockedError::new),
    ACCOUNT_BLOCKED("GE9KT60O/90AA/1204", AccountBlockedError::new);

    private final String code;
    private final Supplier<AgentBankApiError> error;

    ErrorMapping(String code, Supplier<AgentBankApiError> error) {
        this.code = code;
        this.error = error;
    }

    public String getCode() {
        return code;
    }

    public AgentBankApiError getError() {
        return error.get();
    }

    public boolean contains(String detail) {
        return StringUtils.contains(detail.toUpperCase(), code);
    }
}
