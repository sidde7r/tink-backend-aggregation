package se.tink.backend.aggregation.nxgen.controllers.authentication.oauth;

import java.util.Optional;

public interface OAuth2TokenStorage {

    Optional<OAuth2Token> fetchToken();

    void storeToken(final OAuth2Token token);
}
