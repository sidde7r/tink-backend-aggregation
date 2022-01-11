package se.tink.agent.sdk.steppable_execution.base_step;

import java.util.Optional;
import javax.annotation.Nullable;
import lombok.AllArgsConstructor;
import se.tink.agent.sdk.operation.StaticBankCredentials;
import se.tink.agent.sdk.operation.User;
import se.tink.agent.sdk.storage.Storage;
import se.tink.agent.sdk.user_interaction.UserResponseData;

@AllArgsConstructor
public class StepRequest implements StepRequestBase, StepRequestUserResponse {
    private final User user;
    private final StaticBankCredentials staticBankCredentials;

    // This storage/state should be used by steps to not interfere with the agent's storage keys.
    private final Storage stepStorage;

    private final Storage agentStorage;

    @Nullable private final UserResponseData userResponseData;

    @Override
    public User getUser() {
        return this.user;
    }

    @Override
    public StaticBankCredentials getStaticBankCredentials() {
        return this.staticBankCredentials;
    }

    @Override
    public Storage getStepStorage() {
        return this.stepStorage;
    }

    @Override
    public Storage getAgentStorage() {
        return this.agentStorage;
    }

    @Override
    public Optional<UserResponseData> getUserResponseData() {
        return Optional.ofNullable(this.userResponseData);
    }
}
