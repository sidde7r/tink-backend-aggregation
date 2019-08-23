package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationProgressiveController;

final class PostRedirectStep implements AuthenticationStep {

    private final OAuth2AuthenticationProgressiveController authenticator;

    PostRedirectStep(final OAuth2AuthenticationProgressiveController authenticator) {
        this.authenticator = authenticator;
    }

    @Override
    public AuthenticationResponse respond(final AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {

        authenticator.collect(request.getCallbackData());

        return AuthenticationResponse.empty();
    }
}
