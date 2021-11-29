package se.tink.agent.sdk.authentication.authenticators.oauth2;

import se.tink.agent.sdk.models.authentication.RefreshableAccessToken;

public interface RefreshAccessToken {
    RefreshableAccessToken refreshAccessToken(String refreshToken);
}
