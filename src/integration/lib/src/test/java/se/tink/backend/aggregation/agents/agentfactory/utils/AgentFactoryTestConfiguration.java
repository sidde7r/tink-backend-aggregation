package se.tink.backend.aggregation.agents.agentfactory.utils;

import java.util.List;
import lombok.Getter;

@Getter
public class AgentFactoryTestConfiguration {
    private List<String> ignoredAgentsForCapabilityTest;
    private List<String> ignoredAgentsForInitialisationTest;
}
