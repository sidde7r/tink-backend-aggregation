package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.codetoken;

import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import se.tink.backend.agents.rpc.Credentials;

@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class NemIdAuthorizeWithCodeTokenStep {

    private final NemIdCodeTokenAskUserForCodeStep askUserForCodeStep;
    private final NemIdCodeTokenGetTokenStep getTokenStep;

    public String getNemIdTokenWithCodeTokenAuth(Credentials credentials) {
        String code = askUserForCodeStep.askForCodeAndValidateResponse(credentials);
        return getTokenStep.enterCodeAndGetToken(code);
    }
}
