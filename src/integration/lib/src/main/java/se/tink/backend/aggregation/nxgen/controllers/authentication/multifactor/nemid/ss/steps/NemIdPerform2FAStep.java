package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps;

import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.NEM_ID_PREFIX;

import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemId2FAMethod;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.codeapp.NemIdAuthorizeWithCodeAppStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.steps.codecard.NemIdAuthorizeWithCodeCardStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.exception.NemIdError;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class NemIdPerform2FAStep {

    private final NemIdAuthorizeWithCodeAppStep authorizeWithCodeAppStep;
    private final NemIdAuthorizeWithCodeCardStep authorizeWithCodeCardStep;

    public String authenticateToGetNemIdToken(
            NemId2FAMethod nemId2FAMethod, Credentials credentials) {

        switch (nemId2FAMethod) {
            case CODE_APP:
                return authorizeWithCodeAppStep.getNemIdTokenWithCodeAppAuth(credentials);
            case CODE_CARD:
                return authorizeWithCodeCardStep.getNemIdTokenWithCodeCardAuth(credentials);
            case CODE_TOKEN:
                throw NemIdError.CODE_TOKEN_NOT_SUPPORTED.exception(
                        NEM_ID_PREFIX + " User has code token.");
            default:
                throw new IllegalStateException("Unknown NemId 2FA method");
        }
    }
}
