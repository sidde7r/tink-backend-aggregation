package se.tink.backend.aggregation.nxgen.controllers.authentication.step;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStepResponse;

/**
 * Interface designed for service callback for automatic authentication steps which don't have any
 * callback data.
 */
public interface CallbackProcessorEmpty {

    AuthenticationStepResponse process() throws AuthenticationException, AuthorizationException;
}
