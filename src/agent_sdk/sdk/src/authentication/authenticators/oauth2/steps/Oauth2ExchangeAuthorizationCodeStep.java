package se.tink.agent.sdk.authentication.authenticators.oauth2.steps;

import se.tink.agent.sdk.authentication.authenticators.oauth2.ExchangeAuthorizationCode;
import se.tink.agent.sdk.authentication.authenticators.oauth2.HandleCallbackDataError;
import se.tink.agent.sdk.authentication.authenticators.oauth2.Oauth2Constants;
import se.tink.agent.sdk.authentication.authenticators.oauth2.Oauth2Utils;
import se.tink.agent.sdk.authentication.authenticators.oauth2.RefreshableAccessTokenAndConsentLifetime;
import se.tink.agent.sdk.authentication.new_consent.NewConsentRequest;
import se.tink.agent.sdk.authentication.new_consent.NewConsentStep;
import se.tink.agent.sdk.authentication.new_consent.response.NewConsentResponse;
import se.tink.agent.sdk.user_interaction.UserResponseData;

public class Oauth2ExchangeAuthorizationCodeStep implements NewConsentStep {
    private final HandleCallbackDataError agentHandleCallbackDataError;
    private final ExchangeAuthorizationCode agentExchangeAuthorizationCode;

    public Oauth2ExchangeAuthorizationCodeStep(
            HandleCallbackDataError agentHandleCallbackDataError,
            ExchangeAuthorizationCode agentExchangeAuthorizationCode) {
        this.agentHandleCallbackDataError = agentHandleCallbackDataError;
        this.agentExchangeAuthorizationCode = agentExchangeAuthorizationCode;
    }

    @Override
    public NewConsentResponse execute(NewConsentRequest request) {
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

        RefreshableAccessTokenAndConsentLifetime result =
                agentExchangeAuthorizationCode.exchangeAuthorizationCode(authorizationCode);

        if (!result.getToken().isAccessTokenValid()) {
            throw new IllegalStateException("Access token is invalid.");
        }

        request.getAgentStorage().putAccessToken(result.getToken());
        return NewConsentResponse.done(result.getConsentLifetime());
    }
}
