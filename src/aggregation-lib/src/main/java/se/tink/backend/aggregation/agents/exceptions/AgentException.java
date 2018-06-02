package se.tink.backend.aggregation.agents.exceptions;

import se.tink.backend.aggregation.agents.exceptions.errors.AgentError;

interface AgentException {
    AgentError getError();
}
