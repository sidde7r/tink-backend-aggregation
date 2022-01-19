package se.tink.agent.sdk.authentication.authenticators.oauth2_decoupled_app.steps;

import java.util.Objects;
import se.tink.agent.sdk.authentication.authenticators.oauth2.AccessTokenAndConsentLifetime;
import se.tink.agent.sdk.authentication.authenticators.oauth2_decoupled_app.FetchAccessToken;
import se.tink.agent.sdk.authentication.base_steps.NewConsentStep;
import se.tink.agent.sdk.authentication.consent.ConsentLifetime;
import se.tink.agent.sdk.steppable_execution.base_step.StepRequest;
import se.tink.agent.sdk.steppable_execution.interactive_step.response.InteractiveStepResponse;

public class Oauth2FetchAccessToken extends NewConsentStep {
    private final FetchAccessToken agentFetchAccessToken;

    public Oauth2FetchAccessToken(FetchAccessToken agentFetchAccessToken) {
        this.agentFetchAccessToken = agentFetchAccessToken;
    }

    @Override
    public InteractiveStepResponse<ConsentLifetime> execute(StepRequest<Void> request) {
        AccessTokenAndConsentLifetime result = this.agentFetchAccessToken.fetchAccessToken();
        if (Objects.isNull(result.getToken()) || !result.getToken().isValid()) {
            throw new IllegalStateException("AccessToken is invalid.");
        }
        request.getAgentStorage().putOauth2Token(result.getToken());

        return InteractiveStepResponse.done(result.getConsentLifetime());
    }
}
