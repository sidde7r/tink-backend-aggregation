package se.tink.agent.sdk.environment;

import com.google.common.collect.ImmutableList;
import se.tink.agent.sdk.user_interaction.ThirdPartyAppInfo;
import se.tink.agent.sdk.user_interaction.UserInteraction;

public interface MultifactorAuthenticationState {
    String getState();

    UserInteraction<ThirdPartyAppInfo> intoUserInteraction(ThirdPartyAppInfo appInfo);
    // TODO: fields type instead of String.
    UserInteraction<ImmutableList<String>> intoUserInteraction(ImmutableList<String> fields);
}
