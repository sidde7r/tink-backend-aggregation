package se.tink.backend.aggregation.agents.banks.crosskey;

import se.tink.backend.aggregation.agents.exceptions.agent.AgentError;
import se.tink.libraries.i18n_aggregation.LocalizableEnum;

public interface CrossKeyError extends LocalizableEnum {
    AgentError getAgentError();
}
