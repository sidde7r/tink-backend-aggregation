package se.tink.agent.runtime.operation;

import se.tink.agent.sdk.operation.MultifactorAuthenticationState;
import se.tink.agent.sdk.user_interaction.SupplementalInformation;
import se.tink.agent.sdk.user_interaction.ThirdPartyAppInfo;
import se.tink.agent.sdk.user_interaction.UserInteraction;
import se.tink.agent.sdk.user_interaction.UserInteractionBuilder;

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
    public UserInteraction<SupplementalInformation> intoUserInteraction(
            SupplementalInformation supplementalInformation) {
        return setUserResponseRequired(
                UserInteraction.supplementalInformation(supplementalInformation));
    }

    private <T> UserInteraction<T> setUserResponseRequired(UserInteractionBuilder<T> builder) {
        return builder.userResponseRequired(String.format(UNIQUE_PREFIX_TPCB, this.state)).build();
    }
}
