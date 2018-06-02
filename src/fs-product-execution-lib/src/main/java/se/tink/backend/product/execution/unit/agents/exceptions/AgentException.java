package se.tink.backend.product.execution.unit.agents.exceptions;

import se.tink.backend.product.execution.unit.agents.exceptions.errors.AgentError;

interface AgentException {
    AgentError getError();
}
