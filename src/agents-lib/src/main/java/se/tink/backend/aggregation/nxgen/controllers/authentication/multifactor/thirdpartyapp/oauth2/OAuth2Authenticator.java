package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2;

import java.util.Optional;
import se.tink.backend.aggregation.nxgen.http.URL;

public interface OAuth2Authenticator {

    interface Configuration {
        // The controller will populate the following query parameters (as specified in rfc6749 4.1.1.):
        //  - response_type
        //  - client_id
        //  - redirect_uri
        //  - scope (OPTIONAL)
        //  - state
        //
        // Any additional parameters must be added by the authenticator implementation before returning the URL
        // in this method call.
        URL getAuthorizationUrl();

        // E.g. `code`.
        String getResponseType();

        // Assigned by the bank.
        String getClientId();

        // As specified in the bank's API documentation (if present).
        Optional<String> getScope();


        // The controller will populate the following query parameters (as specified in rfc6749 4.1.3.):
        //  - grant_type
        //  - code
        //  - redirect_uri
        //  - client_id
        //
        // Any additional parameters must be added by the authenticator implementation before returning the URL
        // in this method call.
        URL getAccessTokenUrl();

        // E.g. `authorization_code`
        String getGrantType();
    }

    Configuration getConfiguration();
    OAuth2TokenResponse performAccessTokenRequest(URL accessTokenUrl);
    void storeToken(OAuth2Token token);
    Optional<OAuth2Token> loadToken();
}
