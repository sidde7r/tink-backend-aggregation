package se.tink.agent.sdk.authentication.authenticators.thirdparty_app;

import se.tink.agent.sdk.authentication.common_steps.GetConsentLifetime;
import se.tink.agent.sdk.authentication.common_steps.VerifyBankConnection;

public interface ThirdPartyAppAuthenticator
        extends VerifyBankConnection,
                ThirdPartyAppInitAuthentication,
                ThirdPartyAppGetAppInfo,
                ThirdPartyAppPollStatus,
                GetConsentLifetime {

    String STATE_KEY_REFERENCE = "third_party_app_reference";
    String STATE_KEY_COUNTER = "third_party_app_counter";
}
