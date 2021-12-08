package se.tink.agent.sdk.authentication.authenticators.oauth2_decoupled_swedish_mobile_bankid;

import se.tink.agent.sdk.authentication.authenticators.oauth2.RefreshAccessToken;
import se.tink.agent.sdk.authentication.authenticators.oauth2_decoupled_app.FetchAccessToken;
import se.tink.agent.sdk.authentication.authenticators.swedish_mobile_bankid.SwedishMobileBankIdGetAutostartToken;
import se.tink.agent.sdk.authentication.authenticators.thirdparty_app.ThirdPartyAppInitAuthentication;
import se.tink.agent.sdk.authentication.authenticators.thirdparty_app.ThirdPartyAppPollStatus;
import se.tink.agent.sdk.authentication.common_steps.VerifyBankConnection;

public interface Oauth2DecoupledSwedishMobileBankIdAuthenticator
        extends RefreshAccessToken,
                VerifyBankConnection,
                ThirdPartyAppInitAuthentication,
                SwedishMobileBankIdGetAutostartToken,
                ThirdPartyAppPollStatus,
                FetchAccessToken {}
