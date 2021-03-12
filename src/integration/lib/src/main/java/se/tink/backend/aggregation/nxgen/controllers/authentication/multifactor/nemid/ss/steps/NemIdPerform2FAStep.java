package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps;

import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.NEM_ID_PREFIX;

import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemId2FAMethod;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.codeapp.NemIdAuthorizeWithCodeAppStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.codecard.NemIdAuthorizeWithCodeCardStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.codetoken.NemIdAuthorizeWithCodeTokenStep;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class NemIdPerform2FAStep {

    private final NemIdAuthorizeWithCodeAppStep authorizeWithCodeAppStep;
    private final NemIdAuthorizeWithCodeCardStep authorizeWithCodeCardStep;
    private final NemIdAuthorizeWithCodeTokenStep authorizeWithCodeTokenStep;

    public String authenticateToGetNemIdToken(
            NemId2FAMethod nemId2FAMethod, Credentials credentials) {

        switch (nemId2FAMethod) {
            case CODE_APP:
                log.info("{}[NemIdPerform2FAStep] Code app chosen", NEM_ID_PREFIX);
                return authorizeWithCodeAppStep.getNemIdTokenWithCodeAppAuth(credentials);
            case CODE_CARD:
                log.info("{}[NemIdPerform2FAStep] Code card chosen", NEM_ID_PREFIX);
                return authorizeWithCodeCardStep.getNemIdTokenWithCodeCardAuth(credentials);
            case CODE_TOKEN:
                log.info("{}[NemIdPerform2FAStep] Code token chosen", NEM_ID_PREFIX);
                return authorizeWithCodeTokenStep.getNemIdTokenWithCodeTokenAuth(credentials);
            default:
                throw new IllegalStateException("Unknown NemId 2FA method");
        }
    }
}
