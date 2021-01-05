package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.progressive;

import java.util.Arrays;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStrongAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.ProgressiveTypedAuthenticator;

public class ThirdPartyAppAuthenticationProgressiveController
        implements ProgressiveTypedAuthenticator {

    private final ThirdPartyAppStrongAuthenticator authenticator;

    public ThirdPartyAppAuthenticationProgressiveController(
            ThirdPartyAppStrongAuthenticator authenticator) {
        this.authenticator = authenticator;
    }

    @Override
    public CredentialsTypes getType() {
        return CredentialsTypes.THIRD_PARTY_APP;
    }

    @Override
    public Iterable<AuthenticationStep> authenticationSteps() {
        return Arrays.asList(
                new OpenThirdPartyAppStep(authenticator),
                new RedirectStep(authenticator),
                new PostRedirectStep(authenticator));
    }
}
