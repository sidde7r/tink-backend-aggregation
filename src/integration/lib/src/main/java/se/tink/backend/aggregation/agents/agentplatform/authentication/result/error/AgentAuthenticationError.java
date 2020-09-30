package se.tink.backend.aggregation.agents.agentplatform.authentication.result.error;

import lombok.EqualsAndHashCode;
import se.tink.backend.aggregation.agents.exceptions.agent.AgentError;
import se.tink.backend.aggregation.agents.exceptions.agent.AgentException;
import se.tink.backend.aggregation.agentsplatform.framework.error.AgentBankApiError;
import se.tink.backend.aggregation.agentsplatform.framework.error.ServerError;
import se.tink.libraries.i18n.LocalizableKey;

@EqualsAndHashCode
public class AgentAuthenticationError implements AgentError {

    private static final String METHOD_IS_NOT_ALLOWED_TO_USE_MESSAGE =
            "Method is not allowed to use";

    private final AgentBankApiError agentBankApiError;

    public AgentAuthenticationError(AgentBankApiError agentBankApiError) {
        this.agentBankApiError = agentBankApiError;
    }

    @Override
    public String name() {
        return agentBankApiError.getClass().getSimpleName();
    }

    @Override
    public LocalizableKey userMessage() {
        return new LocalizableKey(buildExceptionMessage());
    }

    @Override
    public AgentException exception() {
        if (agentBankApiError instanceof ServerError) {
            return new ServerErrorAgentAuthenticationException(this, buildExceptionMessage());
        }
        return new AgentAuthenticationException(this, buildExceptionMessage());
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
}
