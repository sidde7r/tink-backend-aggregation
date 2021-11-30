package se.tink.agent.sdk.authentication.authenticators.oauth2;

import se.tink.agent.sdk.authentication.common_steps.VerifyBankConnection;

public interface Oauth2Authenticator
        extends BuildAuthorizationAppUrl,
                HandleCallbackDataError,
                ExchangeAuthorizationCode,
                RefreshAccessToken,
                VerifyBankConnection {}
