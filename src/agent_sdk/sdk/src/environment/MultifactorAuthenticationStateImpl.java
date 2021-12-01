package se.tink.agent.sdk.environment;

import com.google.common.collect.ImmutableList;
import se.tink.agent.sdk.user_interaction.ThirdPartyAppInfo;
import se.tink.agent.sdk.user_interaction.UserInteraction;
import se.tink.agent.sdk.user_interaction.UserInteractionBuilder;
import se.tink.backend.agents.rpc.Field;

// TODO: move this class
public class MultifactorAuthenticationStateImpl implements MultifactorAuthenticationState {
    private static final String UNIQUE_PREFIX_TPCB = "tpcb_%s";

    private final String state;

    public MultifactorAuthenticationStateImpl(String state) {
        this.state = state;
    }

    @Override
    public String getState() {
        return state;
    }

    @Override
    public UserInteraction<ThirdPartyAppInfo> intoUserInteraction(ThirdPartyAppInfo appInfo) {
        return setUserResponseRequired(UserInteraction.thirdPartyApp(appInfo));
    }

    @Override
    public UserInteraction<ImmutableList<Field>> intoUserInteraction(ImmutableList<Field> fields) {
        return setUserResponseRequired(UserInteraction.supplementalInformation(fields));
    }

    private <T> UserInteraction<T> setUserResponseRequired(UserInteractionBuilder<T> builder) {
        return builder.userResponseRequired(String.format(UNIQUE_PREFIX_TPCB, this.state)).build();
    }
}
