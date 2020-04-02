package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.progressive;

import java.util.concurrent.TimeUnit;
import se.tink.backend.aggregation.nxgen.controllers.authentication.*;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStrongAuthenticator;

final class RedirectStep implements AuthenticationStep {

    private final ThirdPartyAppStrongAuthenticator authenticator;

    RedirectStep(final ThirdPartyAppStrongAuthenticator authenticator) {
        this.authenticator = authenticator;
    }

    @Override
    public AuthenticationStepResponse execute(final AuthenticationRequest request) {

        SupplementalWaitRequest waitRequest =
                new SupplementalWaitRequest(
                        authenticator.getStrongAuthenticationStateSupplementalKey(),
                        authenticator.getWaitForMinutes(),
                        TimeUnit.MINUTES);

        return AuthenticationStepResponse.requestForSupplementInformation(
                new SupplementInformationRequester.Builder()
                        .withSupplementalWaitRequest(waitRequest)
                        .build());
    }
}
