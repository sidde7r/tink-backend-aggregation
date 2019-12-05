package se.tink.backend.aggregation.nxgen.controllers.authentication.step;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;

public interface SingleFieldCallbackProcessor {
    void process(final String value) throws AuthenticationException;
}
