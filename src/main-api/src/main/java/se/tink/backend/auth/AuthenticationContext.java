package se.tink.backend.auth;

import java.util.Map;
import java.util.Optional;
import se.tink.backend.core.Client;
import se.tink.backend.core.ClientType;
import se.tink.backend.core.User;
import se.tink.libraries.auth.HttpAuthenticationMethod;
import se.tink.backend.core.oauth2.OAuth2Client;

/**
 * The context in which this Request is made. Contains information about the authentication but also
 * useful surrounding concepts/details like if the user was created via one of our OAuth2 clients, which remote address
 * the end user have or which Tink Client was used.
 */
public interface AuthenticationContext {

    Optional<OAuth2Client> getOAuth2Client();

    Optional<Client> getClient();

    Optional<String> getUserAgent();

    Optional<String> getRemoteAddress();

    Optional<String> getUserDeviceId();

    boolean isAuthenticated();

    boolean isAdministrativeMode();

    User getUser();

    HttpAuthenticationMethod getHttpAuthenticationMethod();

    Map<String, String> getMetadata();

    default ClientType getClientType() {
        if (getUserAgent() == null || !getUserAgent().isPresent()) {
            return ClientType.OTHER;
        }

        if (getUserAgent().get().contains("iOS")) {
            return ClientType.IOS;
        } else if (getUserAgent().get().contains("Android")) {
            return ClientType.ANDROID;
        }

        return ClientType.OTHER;
    }
}
