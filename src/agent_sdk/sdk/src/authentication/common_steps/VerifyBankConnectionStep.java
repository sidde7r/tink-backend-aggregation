package se.tink.agent.sdk.authentication.common_steps;

import se.tink.agent.sdk.authentication.existing_consent.ConsentStatus;
import se.tink.agent.sdk.authentication.existing_consent.ExistingConsentRequest;
import se.tink.agent.sdk.authentication.existing_consent.ExistingConsentResponse;
import se.tink.agent.sdk.authentication.existing_consent.ExistingConsentStep;

public class VerifyBankConnectionStep implements ExistingConsentStep {
    private final VerifyBankConnection agentVerifyBankConnection;

    public VerifyBankConnectionStep(VerifyBankConnection agentVerifyBankConnection) {
        this.agentVerifyBankConnection = agentVerifyBankConnection;
    }

    @Override
    public ExistingConsentResponse execute(ExistingConsentRequest request) {
        ConsentStatus consentStatus = agentVerifyBankConnection.verifyBankConnection();
        return ExistingConsentResponse.done(consentStatus);
    }
}
