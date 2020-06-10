package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based;

import java.util.Optional;

public interface AccessTokenStorage<T> {

    Optional<T> getToken();

    Optional<T> getTokenFromSession();

    void storeToken(T token);

    void storeTokenInSession(T token);

    void rotateToken(T token);

    void clearToken();
}
