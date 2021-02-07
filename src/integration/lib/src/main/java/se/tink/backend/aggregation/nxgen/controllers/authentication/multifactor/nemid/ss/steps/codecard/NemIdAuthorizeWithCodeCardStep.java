package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.codecard;

import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class NemIdAuthorizeWithCodeCardStep {

    private final NemIdCodeCardAskUserForCodeStep askUserForCodeStep;
    private final NemIdCodeCardGetTokenStep getTokenStep;

    public String getNemIdTokenWithCodeCardAuth(Credentials credentials) {
        String code = askUserForCodeStep.askForCodeAndValidateResponse(credentials);
        return getTokenStep.enterCodeAndGetToken(code);
    }
}
