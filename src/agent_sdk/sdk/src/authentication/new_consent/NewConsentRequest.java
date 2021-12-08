package se.tink.agent.sdk.authentication.new_consent;

import java.util.Optional;
import javax.annotation.Nullable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import se.tink.agent.sdk.operation.StaticBankCredentials;
import se.tink.agent.sdk.operation.User;
import se.tink.agent.sdk.storage.Storage;
import se.tink.agent.sdk.user_interaction.UserResponseData;

@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class NewConsentRequest {
    private final User user;
    private final StaticBankCredentials staticBankCredentials;
    @Nullable private final UserResponseData userResponseData;

    // This storage/state should be used by authentication steps to not interfere with agent's
    // storage keys.
    private final Storage authenticationStorage;

    private final Storage agentStorage;

    public User getUser() {
        return user;
    }

    public StaticBankCredentials getStaticBankCredentials() {
        return staticBankCredentials;
    }

    public Optional<UserResponseData> getUserResponseData() {
        return Optional.ofNullable(userResponseData);
    }

    public Storage getAuthenticationStorage() {
        return authenticationStorage;
    }

    public Storage getAgentStorage() {
        return agentStorage;
    }
}
