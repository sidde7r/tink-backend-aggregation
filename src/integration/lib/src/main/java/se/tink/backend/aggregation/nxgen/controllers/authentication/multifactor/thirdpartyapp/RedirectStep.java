package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
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
    public Optional<SupplementInformationRequester> execute(final AuthenticationRequest request) {

        SupplementalWaitRequest waitRequest =
                new SupplementalWaitRequest(
                        authenticator.getStrongAuthenticationStateSupplementalKey(),
                        authenticator.getWaitForMinutes(),
                        TimeUnit.MINUTES);

        return Optional.of(
                new SupplementInformationRequester.Builder()
                        .withSupplementalWaitRequest(waitRequest)
                        .build());
    }
}
