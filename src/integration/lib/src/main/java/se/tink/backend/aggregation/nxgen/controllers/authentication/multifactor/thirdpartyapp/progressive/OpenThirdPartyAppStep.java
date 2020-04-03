package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.progressive;

import com.google.common.base.Preconditions;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SupplementInformationRequester;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStrongAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;

final class OpenThirdPartyAppStep implements AuthenticationStep {

    private final ThirdPartyAppStrongAuthenticator authenticator;

    OpenThirdPartyAppStep(final ThirdPartyAppStrongAuthenticator authenticator) {
        this.authenticator = authenticator;
    }

    @Override
    public AuthenticationStepResponse execute(final AuthenticationRequest request) {

        ThirdPartyAppAuthenticationPayload payload = authenticator.getAppPayload();
        Preconditions.checkNotNull(payload);

        return AuthenticationStepResponse.requestForSupplementInformation(
                new SupplementInformationRequester.Builder()
                        .withThirdPartyAppAuthenticationPayload(payload)
                        .build());
    }
}
