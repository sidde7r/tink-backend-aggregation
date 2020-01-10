package se.tink.backend.aggregation.nxgen.controllers.authentication.step;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;

/**
 * Interface designed for service callback for automatic authentication steps which don't have any
 * callback data.
 */
public interface CallbackProcessorEmpty {

    void process() throws AuthenticationException;
}
