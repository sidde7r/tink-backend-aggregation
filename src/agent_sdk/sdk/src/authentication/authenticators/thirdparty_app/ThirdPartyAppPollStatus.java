package se.tink.agent.sdk.authentication.authenticators.thirdparty_app;

import se.tink.agent.sdk.storage.Reference;

public interface ThirdPartyAppPollStatus {
    ThirdPartyAppResult pollThirdPartyAppStatus(Reference reference);
}
