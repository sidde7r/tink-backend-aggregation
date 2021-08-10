package se.tink.backend.aggregation.nxgen.agents.componentproviders.storage;

import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.nxgen.storage.AgentTemporaryStorage;

@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class AgentTemporaryStorageProviderImpl implements AgentTemporaryStorageProvider {

    private final AgentTemporaryStorage agentTemporaryStorage;

    @Override
    public AgentTemporaryStorage getAgentTemporaryStorage() {
        return agentTemporaryStorage;
    }
}
