package se.tink.backend.aggregation.agents.agentcapabilities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import se.tink.backend.aggregation.agents.CapabilityExecutor;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.client.provider_configuration.rpc.Capability;

@Getter
@AllArgsConstructor
public class Mapping {

    private final Class<? extends CapabilityExecutor> executorClass;
    private final Capability capability;

    public boolean canMap(Class<? extends Agent> agentClass) {
        return executorClass.isAssignableFrom(agentClass);
    }
}
