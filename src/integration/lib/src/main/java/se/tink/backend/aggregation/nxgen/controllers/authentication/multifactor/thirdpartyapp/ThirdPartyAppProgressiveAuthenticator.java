package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp;

import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.ProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.libraries.i18n.LocalizableKey;

public interface ThirdPartyAppProgressiveAuthenticator<T> extends ProgressiveAuthenticator {
    ThirdPartyAppResponse<T> init();

    ThirdPartyAppResponse<T> collect(T reference)
            throws AuthenticationException, AuthorizationException;

    ThirdPartyAppAuthenticationPayload getAppPayload();

    Optional<LocalizableKey> getUserErrorMessageFor(ThirdPartyAppStatus status);
}
