package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp;

import com.google.common.base.Preconditions;
import java.util.Optional;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SupplementInformationRequester;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;

final class OpenThirdPartyAppStep implements AuthenticationStep {

    private final ThirdPartyAppStrongAuthenticator authenticator;

    OpenThirdPartyAppStep(final ThirdPartyAppStrongAuthenticator authenticator) {
        this.authenticator = authenticator;
    }

    @Override
    public Optional<SupplementInformationRequester> execute(final AuthenticationRequest request) {

        ThirdPartyAppAuthenticationPayload payload = authenticator.getAppPayload();
        Preconditions.checkNotNull(payload);

        return Optional.of(
                new SupplementInformationRequester.Builder()
                        .withThirdPartyAppAuthenticationPayload(payload)
                        .build());
    }
}
