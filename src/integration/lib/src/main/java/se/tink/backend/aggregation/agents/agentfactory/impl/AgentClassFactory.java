package se.tink.backend.aggregation.agents.agentfactory.impl;

import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.agent.Agent;

public class AgentClassFactory {

    public static final String DEFAULT_AGENT_PACKAGE_CLASS_PREFIX =
            "se.tink.backend.aggregation.agents";

    public static Class<? extends Agent> getAgentClass(Provider provider)
            throws ClassNotFoundException {
        return getAgentClass(provider.getClassName());
    }

    @SuppressWarnings("unchecked")
    public static Class<? extends Agent> getAgentClass(String className)
            throws ClassNotFoundException {
        return (Class<? extends Agent>)
                Class.forName(DEFAULT_AGENT_PACKAGE_CLASS_PREFIX + "." + className);
    }
}
