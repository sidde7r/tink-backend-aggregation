package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp;

import com.google.common.base.Preconditions;
import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
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
    public SupplementInformationRequester respond(final AuthenticationRequest request) {

        ThirdPartyAppAuthenticationPayload payload = authenticator.getAppPayload();
        Preconditions.checkNotNull(payload);

        return SupplementInformationRequester.openThirdPartyApp(payload);
    }

    @Override
    public Optional<SupplementInformationRequester> execute(
            AuthenticationRequest request, Object persistentData)
            throws AuthenticationException, AuthorizationException {
        throw new AssertionError("Not yet implemented");
    }
}
