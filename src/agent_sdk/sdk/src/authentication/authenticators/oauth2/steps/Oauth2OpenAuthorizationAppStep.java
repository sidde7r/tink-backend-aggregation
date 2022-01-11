package se.tink.agent.sdk.authentication.authenticators.oauth2.steps;

import se.tink.agent.sdk.authentication.authenticators.oauth2.BuildAuthorizationAppUrl;
import se.tink.agent.sdk.operation.MultifactorAuthenticationState;
import se.tink.agent.sdk.steppable_execution.base_step.BaseStep;
import se.tink.agent.sdk.steppable_execution.base_step.StepRequest;
import se.tink.agent.sdk.steppable_execution.interactive_step.IntermediateStep;
import se.tink.agent.sdk.steppable_execution.interactive_step.response.IntermediateStepResponse;
import se.tink.agent.sdk.user_interaction.ThirdPartyAppInfo;
import se.tink.agent.sdk.user_interaction.UserInteraction;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class Oauth2OpenAuthorizationAppStep extends IntermediateStep {

    private final MultifactorAuthenticationState multifactorAuthenticationState;
    private final BuildAuthorizationAppUrl agentBuildAuthorizationAppUrl;
    private final Class<? extends BaseStep<?>> handleCallbackDataStep;

    public Oauth2OpenAuthorizationAppStep(
            MultifactorAuthenticationState multifactorAuthenticationState,
            BuildAuthorizationAppUrl agentBuildAuthorizationAppUrl,
            Class<? extends BaseStep<?>> handleCallbackDataStep) {
        this.multifactorAuthenticationState = multifactorAuthenticationState;
        this.agentBuildAuthorizationAppUrl = agentBuildAuthorizationAppUrl;
        this.handleCallbackDataStep = handleCallbackDataStep;
    }

    @Override
    public IntermediateStepResponse execute(StepRequest request) {
        URL authorizeUrl =
                this.agentBuildAuthorizationAppUrl.buildAuthorizationAppUrl(
                        this.multifactorAuthenticationState.getState());

        UserInteraction<ThirdPartyAppInfo> thirdPartyAppUserInteraction =
                buildThirdPartyApp(authorizeUrl);

        return IntermediateStepResponse.nextStep(this.handleCallbackDataStep)
                .userInteraction(thirdPartyAppUserInteraction)
                .build();
    }

    private UserInteraction<ThirdPartyAppInfo> buildThirdPartyApp(URL authorizeUrl) {
        ThirdPartyAppInfo thirdPartyAppInfo = ThirdPartyAppInfo.of(authorizeUrl);
        return this.multifactorAuthenticationState.intoUserInteraction(thirdPartyAppInfo);
    }
}
