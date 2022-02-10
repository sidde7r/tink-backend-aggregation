package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp;

import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.libraries.i18n_aggregation.LocalizableKey;

public interface ThirdPartyAppAuthenticator<T> {
    ThirdPartyAppResponse<T> init();

    ThirdPartyAppResponse<T> collect(T reference)
            throws AuthenticationException, AuthorizationException;

    ThirdPartyAppAuthenticationPayload getAppPayload();

    Optional<LocalizableKey> getUserErrorMessageFor(ThirdPartyAppStatus status);
}
