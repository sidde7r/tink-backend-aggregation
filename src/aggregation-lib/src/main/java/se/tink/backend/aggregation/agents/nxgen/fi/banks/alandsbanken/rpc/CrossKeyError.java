package se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.rpc;

import se.tink.backend.aggregation.agents.exceptions.errors.AgentError;
import se.tink.libraries.i18n.LocalizableEnum;

public interface CrossKeyError extends LocalizableEnum {
    AgentError getAgentError();
}
