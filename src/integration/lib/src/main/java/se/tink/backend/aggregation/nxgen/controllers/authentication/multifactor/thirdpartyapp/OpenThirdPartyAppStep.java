package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp;

import com.google.common.base.Preconditions;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;

final class OpenThirdPartyAppStep<T> implements AuthenticationStep {

    private final ThirdPartyAppProgressiveAuthenticator<T> authenticator;

    OpenThirdPartyAppStep(final ThirdPartyAppProgressiveAuthenticator<T> authenticator) {
        this.authenticator = authenticator;
    }

    @Override
    public AuthenticationResponse respond(final AuthenticationRequest request) {

        ThirdPartyAppAuthenticationPayload payload = authenticator.getAppPayload();
        Preconditions.checkNotNull(payload);

        return AuthenticationResponse.openThirdPartyApp(payload);
    }
}
