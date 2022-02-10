package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.passwordandapp;

import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStatus;
import se.tink.libraries.i18n_aggregation.LocalizableKey;

/**
 * This interface is supposed to be used for apps that might, but doesn't have to, be installed on
 * the same device an original request is coming from
 *
 * @param <T>
 */
public interface ExternalAppAuthenticator<T> {
    /**
     * Initiate the Authentication using the External app
     *
     * @return The status of the initiation
     * @throws AuthenticationException
     * @throws AuthorizationException
     */
    ExternalThirdPartyAppResponse<T> init() throws AuthenticationException, AuthorizationException;

    /**
     * Collects the status of ongoing authentication, checking if it was performed
     *
     * @param reference
     * @return
     * @throws AuthenticationException
     * @throws AuthorizationException
     */
    ExternalThirdPartyAppResponse<T> collect(T reference)
            throws AuthenticationException, AuthorizationException;

    Optional<LocalizableKey> getUserErrorMessageFor(ThirdPartyAppStatus status);
}
