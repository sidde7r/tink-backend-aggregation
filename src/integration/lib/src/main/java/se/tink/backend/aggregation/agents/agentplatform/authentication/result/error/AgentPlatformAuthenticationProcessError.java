package se.tink.backend.aggregation.agents.agentplatform.authentication.result.error;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.exceptions.agent.AgentError;
import se.tink.backend.aggregation.agents.exceptions.agent.AgentException;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AgentBankApiError;
import se.tink.libraries.i18n.LocalizableKey;

@EqualsAndHashCode
@AllArgsConstructor
@Getter
public class AgentPlatformAuthenticationProcessError implements AgentError {

    private static final String METHOD_IS_NOT_ALLOWED_TO_USE_MESSAGE =
            "Method is not allowed to use";

    private final AgentBankApiError agentBankApiError;

    @Override
    public String name() {
        return agentBankApiError.getClass().getSimpleName();
    }

    @Override
    public LocalizableKey userMessage() {
        return new LocalizableKey(buildUserMessage());
    }

    @Override
    public AgentException exception() {
        return new AgentPlatformAuthenticationProcessException(this, buildExceptionMessage());
    }

    @Override
    public AgentException exception(String internalMessage) {
        throw new UnsupportedOperationException(METHOD_IS_NOT_ALLOWED_TO_USE_MESSAGE);
    }

    @Override
    public AgentException exception(Throwable cause) {
        throw new UnsupportedOperationException(METHOD_IS_NOT_ALLOWED_TO_USE_MESSAGE);
    }

    @Override
    public AgentException exception(LocalizableKey userMessage) {
        throw new UnsupportedOperationException(METHOD_IS_NOT_ALLOWED_TO_USE_MESSAGE);
    }

    @Override
    public AgentException exception(LocalizableKey userMessage, Throwable cause) {
        throw new UnsupportedOperationException(METHOD_IS_NOT_ALLOWED_TO_USE_MESSAGE);
    }

    private String buildExceptionMessage() {
        StringBuilder sb = new StringBuilder(agentBankApiError.getClass().getSimpleName());
        if (agentBankApiError.getDetails() != null) {
            sb.append(": ").append(agentBankApiError.getDetails().getErrorMessage());
            if (agentBankApiError.getDetails().getErrorCode() != null) {
                sb.append(" [").append(agentBankApiError.getDetails().getErrorCode()).append("]");
            }
        }
        return sb.toString();
    }

    private String buildUserMessage() {
        if (agentBankApiError.getDetails() != null
                && StringUtils.isNotBlank(agentBankApiError.getDetails().getErrorMessage())) {
            return agentBankApiError.getDetails().getErrorMessage();
        }
        return agentBankApiError.getClass().getSimpleName();
    }
}
