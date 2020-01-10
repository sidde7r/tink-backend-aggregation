package se.tink.backend.aggregation.nxgen.controllers.authentication.step;

import java.util.Map;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;

public interface CallbackProcessorMultiData {

    void process(Map<String, String> callbackData)
            throws AuthenticationException, AuthorizationException;
}
