package se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur.authenticator.mainview;

import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur.CajasurSessionState;
import se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur.authenticator.CajasurAuthenticationApiClient;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.CallbackProcessorEmpty;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@AllArgsConstructor
public class MainViewProcessor implements CallbackProcessorEmpty {

    private final CajasurAuthenticationApiClient cajasurApiClient;
    private final SessionStorage sessionStorage;

    @Override
    public AuthenticationStepResponse process()
            throws AuthenticationException, AuthorizationException {
        CajasurSessionState sessionState = CajasurSessionState.getInstance(sessionStorage);
        URL redirectUrl = cajasurApiClient.submitPostLoginForm(sessionState);
        sessionState.saveGlobalPosition(cajasurApiClient.callForGlobalPositionBody(redirectUrl));
        return AuthenticationStepResponse.authenticationSucceeded();
    }
}
