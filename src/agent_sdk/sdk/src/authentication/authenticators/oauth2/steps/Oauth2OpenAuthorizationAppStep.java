package se.tink.agent.sdk.authentication.authenticators.oauth2.steps;

import se.tink.agent.sdk.authentication.authenticators.oauth2.BuildAuthorizationAppUrl;
import se.tink.agent.sdk.authentication.new_consent.NewConsentRequest;
import se.tink.agent.sdk.authentication.new_consent.NewConsentStep;
import se.tink.agent.sdk.authentication.new_consent.response.NewConsentResponse;
import se.tink.agent.sdk.operation.MultifactorAuthenticationState;
import se.tink.agent.sdk.user_interaction.ThirdPartyAppInfo;
import se.tink.agent.sdk.user_interaction.UserInteraction;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class Oauth2OpenAuthorizationAppStep implements NewConsentStep {

    private final MultifactorAuthenticationState multifactorAuthenticationState;
    private final BuildAuthorizationAppUrl agentBuildAuthorizationAppUrl;
    private final Class<? extends NewConsentStep> handleCallbackDataStep;

    public Oauth2OpenAuthorizationAppStep(
            MultifactorAuthenticationState multifactorAuthenticationState,
            BuildAuthorizationAppUrl agentBuildAuthorizationAppUrl,
            Class<? extends NewConsentStep> handleCallbackDataStep) {
        this.multifactorAuthenticationState = multifactorAuthenticationState;
        this.agentBuildAuthorizationAppUrl = agentBuildAuthorizationAppUrl;
        this.handleCallbackDataStep = handleCallbackDataStep;
    }

    @Override
    public NewConsentResponse execute(NewConsentRequest request) {
        URL authorizeUrl =
                this.agentBuildAuthorizationAppUrl.buildAuthorizationAppUrl(
                        this.multifactorAuthenticationState.getState());

        UserInteraction<ThirdPartyAppInfo> thirdPartyAppUserInteraction =
                buildThirdPartyApp(authorizeUrl);

        return NewConsentResponse.nextStep(this.handleCallbackDataStep)
                .userInteraction(thirdPartyAppUserInteraction)
                .build();
    }

    private UserInteraction<ThirdPartyAppInfo> buildThirdPartyApp(URL authorizeUrl) {
        ThirdPartyAppInfo thirdPartyAppInfo = ThirdPartyAppInfo.of(authorizeUrl);
        return this.multifactorAuthenticationState.intoUserInteraction(thirdPartyAppInfo);
    }
}
