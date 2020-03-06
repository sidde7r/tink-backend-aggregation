package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based;

import java.util.Optional;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2TokenBase;

public interface AccessTokenProvider<T extends OAuth2TokenBase> {

    T exchangeAuthorizationCode(String code);

    Optional<T> refreshAccessToken(String refreshToken);
}
