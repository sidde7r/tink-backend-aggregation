package se.tink.agent.sdk.authentication.authenticators.oauth2;

import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

public interface RefreshAccessToken {
    OAuth2Token refreshAccessToken(String refreshToken);
}
