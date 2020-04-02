package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.progressive;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStrongAuthenticator;

final class PostRedirectStep implements AuthenticationStep {

    private final ThirdPartyAppStrongAuthenticator authenticator;

    PostRedirectStep(final ThirdPartyAppStrongAuthenticator authenticator) {
        this.authenticator = authenticator;
    }

    @Override
    public AuthenticationStepResponse execute(final AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {

        authenticator.collect(request.getCallbackData());

        return AuthenticationStepResponse.executeNextStep();
    }
}
