package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SupplementInformationRequester;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SupplementalWaitRequest;

final class RedirectStep implements AuthenticationStep {

    private final ThirdPartyAppStrongAuthenticator authenticator;

    RedirectStep(final ThirdPartyAppStrongAuthenticator authenticator) {
        this.authenticator = authenticator;
    }

    @Override
    public SupplementInformationRequester respond(final AuthenticationRequest request) {

        SupplementalWaitRequest waitRequest =
                new SupplementalWaitRequest(
                        authenticator.getStrongAuthenticationStateSupplementalKey(),
                        authenticator.getWaitForMinutes(),
                        TimeUnit.MINUTES);

        return SupplementInformationRequester.requestWaitingForSupplementalInformation(waitRequest);
    }

    @Override
    public Optional<SupplementInformationRequester> execute(
            AuthenticationRequest request, Object persistentData)
            throws AuthenticationException, AuthorizationException {
        throw new AssertionError("Not yet implemented");
    }
}
