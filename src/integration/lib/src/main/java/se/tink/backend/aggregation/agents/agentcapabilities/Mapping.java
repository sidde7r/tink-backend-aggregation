package se.tink.backend.aggregation.agents.agentcapabilities;

import java.util.Objects;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Mapping mapping = (Mapping) o;
        return executorClass.isAssignableFrom(mapping.executorClass)
                && capability == mapping.capability;
    }

    @Override
    public int hashCode() {
        return Objects.hash(executorClass, capability);
    }
}
