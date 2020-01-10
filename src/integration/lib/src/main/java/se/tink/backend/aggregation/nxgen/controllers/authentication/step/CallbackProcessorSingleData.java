package se.tink.backend.aggregation.nxgen.controllers.authentication.step;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;

public interface CallbackProcessorSingleData {
    void process(final String value) throws AuthenticationException, AuthorizationException;
}
