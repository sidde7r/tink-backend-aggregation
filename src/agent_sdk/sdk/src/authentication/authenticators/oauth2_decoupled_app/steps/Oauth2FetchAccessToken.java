package se.tink.agent.sdk.authentication.authenticators.oauth2_decoupled_app.steps;

import se.tink.agent.sdk.authentication.authenticators.oauth2.RefreshableAccessTokenAndConsentLifetime;
import se.tink.agent.sdk.authentication.authenticators.oauth2_decoupled_app.FetchAccessToken;
import se.tink.agent.sdk.authentication.new_consent.NewConsentRequest;
import se.tink.agent.sdk.authentication.new_consent.NewConsentStep;
import se.tink.agent.sdk.authentication.new_consent.response.NewConsentResponse;

public class Oauth2FetchAccessToken implements NewConsentStep {
    private final FetchAccessToken agentFetchAccessToken;

    public Oauth2FetchAccessToken(FetchAccessToken agentFetchAccessToken) {
        this.agentFetchAccessToken = agentFetchAccessToken;
    }

    @Override
    public NewConsentResponse execute(final NewConsentRequest request) {
        RefreshableAccessTokenAndConsentLifetime result =
                this.agentFetchAccessToken.fetchAccessToken();
        if (!result.getToken().isAccessTokenValid()) {
            throw new IllegalStateException("AccessToken is invalid.");
        }
        request.getAgentStorage().putAccessToken(result.getToken());

        return NewConsentResponse.done(result.getConsentLifetime());
    }
}
