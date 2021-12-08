package se.tink.agent.sdk.authentication.authenticators.swedish_mobile_bankid;

import se.tink.agent.sdk.authentication.authenticators.thirdparty_app.ThirdPartyAppInitAuthentication;
import se.tink.agent.sdk.authentication.authenticators.thirdparty_app.ThirdPartyAppPollStatus;
import se.tink.agent.sdk.authentication.common_steps.GetConsentLifetime;
import se.tink.agent.sdk.authentication.common_steps.VerifyBankConnection;

public interface SwedishMobileBankIdAuthenticator
        extends VerifyBankConnection,
                ThirdPartyAppInitAuthentication,
                SwedishMobileBankIdGetAutostartToken,
                ThirdPartyAppPollStatus,
                GetConsentLifetime {}
