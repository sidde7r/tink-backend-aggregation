package se.tink.backend.aggregation.nxgen.controllers.authentication.step;

import java.util.Map;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;

public interface CallbackProcessorMultiDataAndCredentials {

    void process(final Map<String, String> callbackData, final Credentials credentials)
            throws AuthenticationException, AuthorizationException;
}
