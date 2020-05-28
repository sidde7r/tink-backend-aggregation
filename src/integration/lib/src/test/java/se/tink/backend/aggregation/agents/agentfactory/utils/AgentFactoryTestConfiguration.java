package se.tink.backend.aggregation.agents.agentfactory.utils;

import java.util.Set;
import lombok.Getter;

@Getter
public class AgentFactoryTestConfiguration {
    private Set<String> ignoredAgentsForCapabilityTest;
    private Set<String> ignoredAgentsForInitialisationTest;
}
