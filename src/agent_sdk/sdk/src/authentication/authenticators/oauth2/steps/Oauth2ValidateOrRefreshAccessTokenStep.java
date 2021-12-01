package se.tink.agent.sdk.authentication.authenticators.oauth2.steps;

import com.google.common.base.Preconditions;
import java.util.Objects;
import java.util.Optional;
import se.tink.agent.sdk.authentication.authenticators.oauth2.RefreshAccessToken;
import se.tink.agent.sdk.authentication.existing_consent.ConsentStatus;
import se.tink.agent.sdk.authentication.existing_consent.ExistingConsentRequest;
import se.tink.agent.sdk.authentication.existing_consent.ExistingConsentResponse;
import se.tink.agent.sdk.authentication.existing_consent.ExistingConsentStep;
import se.tink.agent.sdk.storage.Storage;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2TokenBase;

public class Oauth2ValidateOrRefreshAccessTokenStep implements ExistingConsentStep {
    private final RefreshAccessToken agentRefreshAccessToken;
    private final Class<? extends ExistingConsentStep> nextStep;

    public Oauth2ValidateOrRefreshAccessTokenStep(
            RefreshAccessToken agentRefreshAccessToken,
            Class<? extends ExistingConsentStep> nextStep) {
        this.agentRefreshAccessToken = agentRefreshAccessToken;
        this.nextStep = nextStep;
    }

    @Override
    public ExistingConsentResponse execute(ExistingConsentRequest request) {
        Storage agentStorage = request.getAgentStorage();

        Optional<OAuth2Token> accessToken = agentStorage.getOauth2Token();

        boolean isAccessTokenValid = accessToken.map(OAuth2TokenBase::isValid).orElse(false);
        if (isAccessTokenValid) {
            // Access token is valid, let's use it!
            return ExistingConsentResponse.step(this.nextStep);
        }

        boolean isRefreshTokenValid = accessToken.map(OAuth2TokenBase::canRefresh).orElse(false);
        if (!isRefreshTokenValid) {
            // The access token is not valid and cannot be refreshed.
            // Go to the next configured step to issue a new access token.
            return ExistingConsentResponse.done(ConsentStatus.EXPIRED);
        }

        return refreshAccessToken(agentStorage, accessToken.get());
    }

    private ExistingConsentResponse refreshAccessToken(
            Storage agentStorage, OAuth2Token accessToken) {
        Preconditions.checkState(accessToken.canRefresh(), "Refresh token must be valid.");
        String refreshToken =
                accessToken
                        .getRefreshToken()
                        // This should not be able to happen.
                        .orElseThrow(() -> new IllegalStateException("Refresh token must exist."));

        OAuth2Token newToken = this.agentRefreshAccessToken.refreshAccessToken(refreshToken);
        if (Objects.isNull(newToken) || !newToken.isValid()) {
            // The agent failed to refresh the access token.
            return ExistingConsentResponse.done(ConsentStatus.EXPIRED);
        }

        OAuth2Token updatedToken = newToken.updateTokenWithOldToken(accessToken);
        agentStorage.putOauth2Token(updatedToken);

        // Continue using the new access token.
        return ExistingConsentResponse.step(nextStep);
    }
}
