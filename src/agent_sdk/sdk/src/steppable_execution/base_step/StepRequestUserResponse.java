package se.tink.agent.sdk.steppable_execution.base_step;

import java.util.Optional;
import se.tink.agent.sdk.user_interaction.UserResponseData;

public interface StepRequestUserResponse {
    Optional<UserResponseData> getUserResponseData();
}
