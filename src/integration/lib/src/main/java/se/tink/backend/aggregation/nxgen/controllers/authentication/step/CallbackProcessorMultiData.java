package se.tink.backend.aggregation.nxgen.controllers.authentication.step;

import java.util.Map;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStepResponse;

public interface CallbackProcessorMultiData {

    AuthenticationStepResponse process(Map<String, String> callbackData)
            throws AuthenticationException, AuthorizationException;
}
