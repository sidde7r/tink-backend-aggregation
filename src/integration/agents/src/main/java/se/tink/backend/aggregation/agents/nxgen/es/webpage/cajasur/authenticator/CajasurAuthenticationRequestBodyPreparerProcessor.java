package se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur.authenticator;

import lombok.AllArgsConstructor;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur.CajasurSessionState;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.CredentialsAuthenticationStep;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@AllArgsConstructor
public class CajasurAuthenticationRequestBodyPreparerProcessor
        implements CredentialsAuthenticationStep.CallbackProcessor {

    private final CajasurAuthenticationApiClient cajasurApiClient;
    private final SessionStorage sessionStorage;

    @Override
    public AuthenticationStepResponse process(Credentials credentials)
            throws AuthenticationException {
        String segmentId = cajasurApiClient.callForSegmentId();
        JavaScriptContext javaScriptContext = cajasurApiClient.encryptObfuscatedLogin();
        String username = credentials.getField(se.tink.backend.agents.rpc.Field.Key.USERNAME);
        String password = credentials.getField(se.tink.backend.agents.rpc.Field.Key.PASSWORD);
        String authenticationRequestBody =
                cajasurApiClient.callForAuthenticationRequestBody(
                        username, password, segmentId, javaScriptContext);
        sessionStorage.put(
                CajasurSessionState.class.getSimpleName(),
                new CajasurSessionState(authenticationRequestBody));
        return AuthenticationStepResponse.executeNextStep();
    }
}
