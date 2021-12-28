package se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur.authenticator.login;

import java.awt.image.BufferedImage;
import lombok.AllArgsConstructor;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur.CajasurSessionState;
import se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur.authenticator.CajasurAuthenticationApiClient;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.CredentialsAuthenticationStep;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@AllArgsConstructor
public class CajasurLoginProcessor implements CredentialsAuthenticationStep.CallbackProcessor {

    private final CajasurAuthenticationApiClient cajasurApiClient;
    private final SessionStorage sessionStorage;

    @Override
    public AuthenticationStepResponse process(Credentials credentials)
            throws AuthenticationException {
        String segmentId = cajasurApiClient.callForSegmentId();
        String obfuscatedLoginJavaScript = cajasurApiClient.encryptObfuscatedLogin();
        BufferedImage passwordVirtualKeyboardImage =
                cajasurApiClient.callForPasswordVirtualKeyboardImage();
        CajasurSessionState.getInstance(sessionStorage)
                .saveLoginResponse(
                        cajasurApiClient.callLogin(
                                new LoginRequestParams(
                                        credentials.getField(
                                                se.tink.backend.agents.rpc.Field.Key.USERNAME),
                                        credentials.getField(
                                                se.tink.backend.agents.rpc.Field.Key.PASSWORD),
                                        segmentId,
                                        obfuscatedLoginJavaScript,
                                        passwordVirtualKeyboardImage)));
        return AuthenticationStepResponse.executeNextStep();
    }
}
