package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.authenticator;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.authenticator.dto.responses.TokenResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public interface PolishApiAuthorizationClient {
    URL getAuthorizeUrl(String state);

    TokenResponse exchangeAuthorizationToken(String accessCode);

    TokenResponse exchangeRefreshToken(String refreshToken);

    TokenResponse exchangeTokenForAis(String refreshToken);
}
