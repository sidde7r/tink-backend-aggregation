package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp;

import java.util.Arrays;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.ProgressiveTypedAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AuthenticationControllerType;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class ThirdPartyAppAuthenticationProgressiveController
        implements ProgressiveTypedAuthenticator, AuthenticationControllerType {

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
    public Iterable<? extends AuthenticationStep> authenticationSteps() {
        return Arrays.asList(
                new OpenThirdPartyAppStep(authenticator),
                new RedirectStep(authenticator),
                new PostRedirectStep(authenticator));
    }

    @Override
    public boolean isManualAuthentication(CredentialsRequest request) {
        // since authenticate always opens the third party app
        return true;
    }
}
