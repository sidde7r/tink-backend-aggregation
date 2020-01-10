package se.tink.backend.aggregation.nxgen.controllers.authentication.step;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;

public interface CallbackProcessorSingleDataAndCredentials {

    void process(final String value, final Credentials credentials)
            throws AuthenticationException, AuthorizationException;
}
