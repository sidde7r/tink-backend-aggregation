package se.tink.agent.sdk.authentication.authenticators.oauth2.steps;

import java.util.Objects;
import se.tink.agent.sdk.authentication.authenticators.oauth2.AccessTokenAndConsentLifetime;
import se.tink.agent.sdk.authentication.authenticators.oauth2.ExchangeAuthorizationCode;
import se.tink.agent.sdk.authentication.authenticators.oauth2.HandleCallbackDataError;
import se.tink.agent.sdk.authentication.authenticators.oauth2.Oauth2Constants;
import se.tink.agent.sdk.authentication.authenticators.oauth2.Oauth2Utils;
import se.tink.agent.sdk.authentication.consent.ConsentLifetime;
import se.tink.agent.sdk.steppable_execution.base_step.StepRequest;
import se.tink.agent.sdk.steppable_execution.interactive_step.InteractiveStep;
import se.tink.agent.sdk.steppable_execution.interactive_step.response.InteractiveStepResponse;
import se.tink.agent.sdk.user_interaction.UserResponseData;

public class Oauth2ExchangeAuthorizationCodeStep extends InteractiveStep<ConsentLifetime> {
    private final HandleCallbackDataError agentHandleCallbackDataError;
    private final ExchangeAuthorizationCode agentExchangeAuthorizationCode;

    public Oauth2ExchangeAuthorizationCodeStep(
            HandleCallbackDataError agentHandleCallbackDataError,
            ExchangeAuthorizationCode agentExchangeAuthorizationCode) {
        this.agentHandleCallbackDataError = agentHandleCallbackDataError;
        this.agentExchangeAuthorizationCode = agentExchangeAuthorizationCode;
    }

    @Override
    public InteractiveStepResponse<ConsentLifetime> execute(StepRequest request) {
        UserResponseData userResponseData =
                request.getUserResponseData()
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Supplemental information was expected."));

        // Let the agent handle errors, if any, or try to capture standard errors.
        agentHandleCallbackDataError.handleCallbackDataError(userResponseData);
        Oauth2Utils.handleCallbackDataError(userResponseData);

        String authorizationCode =
                userResponseData
                        .tryGet(Oauth2Constants.CallbackParams.CODE)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "callbackData did not contain 'code' and no error was handled"));

        AccessTokenAndConsentLifetime result =
                agentExchangeAuthorizationCode.exchangeAuthorizationCode(authorizationCode);

        if (Objects.isNull(result.getToken()) || !result.getToken().isValid()) {
            throw new IllegalStateException("Access token is invalid.");
        }

        request.getAgentStorage().putOauth2Token(result.getToken());
        return InteractiveStepResponse.done(result.getConsentLifetime());
    }
}
