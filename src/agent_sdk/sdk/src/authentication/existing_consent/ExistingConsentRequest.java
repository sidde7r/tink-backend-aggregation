package se.tink.agent.sdk.authentication.existing_consent;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import se.tink.agent.sdk.operation.StaticBankCredentials;
import se.tink.agent.sdk.storage.Storage;

@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ExistingConsentRequest {
    private final StaticBankCredentials staticBankCredentials;

    // This storage/state should be used by authentication steps to not interfere with agent's
    // storage keys.
    private final Storage authenticationStorage;

    private final Storage agentStorage;

    public StaticBankCredentials getStaticBankCredentials() {
        return staticBankCredentials;
    }

    public Storage getAuthenticationStorage() {
        return authenticationStorage;
    }

    public Storage getAgentStorage() {
        return agentStorage;
    }
}
