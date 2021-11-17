package se.tink.backend.aggregation.workers.worker.conditions;

import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.agents.agentfactory.impl.AgentClassFactory;
import se.tink.backend.aggregation.nxgen.agents.SubsequentGenerationAgent;

public class IsPrevGenProvider implements Predicate<Provider> {

    @Override
    public boolean apply(Provider provider) {
        return !isNextGenerationAgent(provider);
    }

    /**
     * Helper method giving info if this is a Provider with a Next Generation Agent.
     *
     * @return a boolean telling if this provider points to a Next Generation Agent.
     */
    private static boolean isNextGenerationAgent(Provider provider) {
        if (provider == null) {
            return false;
        }

        if (Strings.isNullOrEmpty(provider.getClassName())) {
            return false;
        }

        try {
            Class<? extends Agent> agentClass = AgentClassFactory.getAgentClass(provider);
            return SubsequentGenerationAgent.class.isAssignableFrom(agentClass);
        } catch (Exception e) {
            return false;
        }
    }
}
