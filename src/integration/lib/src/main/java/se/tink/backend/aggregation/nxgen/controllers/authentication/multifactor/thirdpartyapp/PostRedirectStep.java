package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp;

import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SupplementInformationRequester;

final class PostRedirectStep implements AuthenticationStep {

    private final ThirdPartyAppStrongAuthenticator authenticator;

    PostRedirectStep(final ThirdPartyAppStrongAuthenticator authenticator) {
        this.authenticator = authenticator;
    }

    @Override
    public Optional<SupplementInformationRequester> execute(final AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {

        authenticator.collect(request.getCallbackData());

        return Optional.empty();
    }
}
