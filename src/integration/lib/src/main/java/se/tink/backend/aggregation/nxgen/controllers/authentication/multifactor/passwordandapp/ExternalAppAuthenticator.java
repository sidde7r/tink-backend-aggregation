package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.passwordandapp;

import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStatus;
import se.tink.libraries.i18n.LocalizableKey;

public interface ExternalAppAuthenticator<T> {
    ExternalThirdPartyAppResponse<T> init() throws AuthenticationException, AuthorizationException;

    void authenticate() throws AuthenticationException, AuthorizationException;

    ExternalThirdPartyAppResponse<T> collect(T reference)
            throws AuthenticationException, AuthorizationException;

    Optional<LocalizableKey> getUserErrorMessageFor(ThirdPartyAppStatus status);
}
