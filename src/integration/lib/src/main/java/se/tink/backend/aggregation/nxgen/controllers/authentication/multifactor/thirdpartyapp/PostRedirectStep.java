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
    public SupplementInformationRequester respond(final AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {

        authenticator.collect(request.getCallbackData());

        return SupplementInformationRequester.empty();
    }

    @Override
    public Optional<SupplementInformationRequester> execute(
            AuthenticationRequest request, Object persistentData)
            throws AuthenticationException, AuthorizationException {
        throw new AssertionError("Not yet implemented");
    }
}
