package se.tink.agent.sdk.operation;

import se.tink.agent.sdk.user_interaction.SupplementalInformation;
import se.tink.agent.sdk.user_interaction.ThirdPartyAppInfo;
import se.tink.agent.sdk.user_interaction.UserInteraction;

public interface MultifactorAuthenticationState {
    String getState();

    UserInteraction<ThirdPartyAppInfo> intoUserInteraction(ThirdPartyAppInfo appInfo);

    UserInteraction<SupplementalInformation> intoUserInteraction(
            SupplementalInformation supplementalInformation);
}
