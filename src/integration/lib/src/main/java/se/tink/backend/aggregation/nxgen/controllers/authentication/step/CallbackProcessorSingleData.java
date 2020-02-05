package se.tink.backend.aggregation.nxgen.controllers.authentication.step;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStepResponse;

public interface CallbackProcessorSingleData {
    AuthenticationStepResponse process(final String value)
            throws AuthenticationException, AuthorizationException;
}
