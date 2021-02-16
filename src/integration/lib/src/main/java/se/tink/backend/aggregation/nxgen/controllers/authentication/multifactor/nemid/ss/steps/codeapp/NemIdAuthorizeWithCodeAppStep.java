package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.codeapp;

import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import se.tink.backend.agents.rpc.Credentials;

@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class NemIdAuthorizeWithCodeAppStep {

    private final NemIdCodeAppAskUserToApproveRequestStep waitForUserToApproveRequestStep;
    private final NemIdCodeAppCollectTokenStep collectTokenStep;

    public String getNemIdTokenWithCodeAppAuth(Credentials credentials) {
        waitForUserToApproveRequestStep.sendCodeAppRequestAndWaitForResponse(credentials);
        return collectTokenStep.collectToken();
    }
}
