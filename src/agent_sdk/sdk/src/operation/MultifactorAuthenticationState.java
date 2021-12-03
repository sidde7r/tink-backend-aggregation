package se.tink.agent.sdk.operation;

import com.google.common.collect.ImmutableList;
import se.tink.agent.sdk.user_interaction.ThirdPartyAppInfo;
import se.tink.agent.sdk.user_interaction.UserInteraction;
import se.tink.backend.agents.rpc.Field;

public interface MultifactorAuthenticationState {
    String getState();

    UserInteraction<ThirdPartyAppInfo> intoUserInteraction(ThirdPartyAppInfo appInfo);

    UserInteraction<ImmutableList<Field>> intoUserInteraction(ImmutableList<Field> fields);
}
