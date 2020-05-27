package se.tink.backend.aggregation.agents.agentfactory.initialisation;

import java.util.List;
import lombok.Getter;

@Getter
public class AgentInitialisationTestConfig {
    private List<String> ignoredAgentsForCapabilityTest;
    private List<String> ignoredAgentsForInitialisationTest;
}
