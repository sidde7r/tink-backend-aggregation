package se.tink.agent.sdk.authentication.authenticators.thirdparty_app;

import se.tink.agent.sdk.storage.Reference;
import se.tink.agent.sdk.user_interaction.ThirdPartyAppInfo;
import se.tink.agent.sdk.user_interaction.UserInteraction;

public interface ThirdPartyAppGetAppInfo {
    UserInteraction<ThirdPartyAppInfo> getThirdPartyAppInfo(Reference reference);
}
