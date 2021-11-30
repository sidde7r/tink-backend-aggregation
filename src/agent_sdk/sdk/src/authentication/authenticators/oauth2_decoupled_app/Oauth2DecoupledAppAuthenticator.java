package se.tink.agent.sdk.authentication.authenticators.oauth2_decoupled_app;

import se.tink.agent.sdk.authentication.authenticators.oauth2.RefreshAccessToken;
import se.tink.agent.sdk.authentication.authenticators.thirdparty_app.ThirdPartyAppGetAppInfo;
import se.tink.agent.sdk.authentication.authenticators.thirdparty_app.ThirdPartyAppInitAuthentication;
import se.tink.agent.sdk.authentication.authenticators.thirdparty_app.ThirdPartyAppPollStatus;
import se.tink.agent.sdk.authentication.common_steps.VerifyBankConnection;

public interface Oauth2DecoupledAppAuthenticator
        extends RefreshAccessToken,
                VerifyBankConnection,
                ThirdPartyAppInitAuthentication,
                ThirdPartyAppGetAppInfo,
                ThirdPartyAppPollStatus,
                FetchAccessToken {}
