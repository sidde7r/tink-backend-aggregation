package se.tink.agent.agents.example.authentication;

import se.tink.agent.sdk.authentication.authenticators.thirdparty_app.ThirdPartyAppAuthenticator;
import se.tink.agent.sdk.authentication.authenticators.thirdparty_app.result.ThirdPartyAppResult;
import se.tink.agent.sdk.authentication.existing_consent.ConsentStatus;
import se.tink.agent.sdk.authentication.new_consent.ConsentLifetime;
import se.tink.agent.sdk.storage.Reference;
import se.tink.agent.sdk.user_interaction.ThirdPartyAppInfo;
import se.tink.agent.sdk.user_interaction.UserInteraction;

public class ExampleThirdPartyAppAuthenticator implements ThirdPartyAppAuthenticator {
    @Override
    public ThirdPartyAppResult initThirdPartyAppAuthentication() {
        return null;
    }

    @Override
    public UserInteraction<ThirdPartyAppInfo> getThirdPartyAppInfo(Reference reference) {
        return UserInteraction.thirdPartyApp(null).build();
    }

    @Override
    public ThirdPartyAppResult pollThirdPartyAppStatus(Reference reference) {
        return null;
    }

    @Override
    public ConsentLifetime getConsentLifetime() {
        return null;
    }

    @Override
    public ConsentStatus verifyBankConnection() {
        return null;
    }
}
