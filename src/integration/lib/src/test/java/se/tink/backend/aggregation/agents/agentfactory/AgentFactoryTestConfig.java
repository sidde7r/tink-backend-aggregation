package se.tink.backend.aggregation.agents.agentfactory;

import java.util.List;
import lombok.Getter;

@Getter
public class AgentFactoryTestConfig {
    private List<String> ignoredAgentsForCapabilityTest;
    private List<String> ignoredAgentsForInitialisationTest;
}
