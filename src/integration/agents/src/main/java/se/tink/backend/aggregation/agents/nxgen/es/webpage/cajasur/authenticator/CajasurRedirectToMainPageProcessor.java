package se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur.authenticator;

import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.CallbackProcessorEmpty;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@AllArgsConstructor
public class CajasurRedirectToMainPageProcessor implements CallbackProcessorEmpty {

    private final CajasurAuthenticationApiClient cajasurApiClient;
    private final SessionStorage sessionStorage;

    @Override
    public AuthenticationStepResponse process()
            throws AuthenticationException, AuthorizationException {
        return null;
    }
}
