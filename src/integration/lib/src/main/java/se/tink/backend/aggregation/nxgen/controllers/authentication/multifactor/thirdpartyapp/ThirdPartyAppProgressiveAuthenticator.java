package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp;

import java.util.Map;
import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.libraries.i18n.LocalizableKey;

public interface ThirdPartyAppProgressiveAuthenticator<T> {
    ThirdPartyAppResponse<T> init();

    ThirdPartyAppResponse<T> collect(Map<String, String> callbackData)
            throws AuthenticationException, AuthorizationException;

    ThirdPartyAppAuthenticationPayload getAppPayload();

    Optional<LocalizableKey> getUserErrorMessageFor(ThirdPartyAppStatus status);
}
