package se.tink.backend.aggregation.agents.exceptions;

import se.tink.backend.aggregation.agents.exceptions.errors.AgentBaseError;

interface AgentException {
    AgentBaseError getError();
}
