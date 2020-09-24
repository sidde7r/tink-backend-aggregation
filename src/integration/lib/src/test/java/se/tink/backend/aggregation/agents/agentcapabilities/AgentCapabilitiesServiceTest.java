package se.tink.backend.aggregation.agents.agentcapabilities;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import se.tink.backend.aggregation.client.provider_configuration.rpc.Capability;

public class AgentCapabilitiesServiceTest {

    @Rule public ErrorCollector collector = new ErrorCollector();

    private static final String EXPECTED_AGENT_CAPABILITIES_FILE_PATH =
            "external/tink_backend/src/provider_configuration/data/seeding/providers/capabilities/agent-capabilities.json";

    private Map<String, Set<Capability>> capabilities;

    private Function<Entry<String, List<String>>, Set<String>> convertFromListToSet =
            entry -> new HashSet<>(entry.getValue());

    @Before
    public void init() {
        capabilities = new AgentCapabilitiesService().getAgentsCapabilities();
        removeTestAgents();
    }

    private void removeTestAgents() {
        capabilities.remove("agentcapabilities.TestAgentImplementingExecutors");
        capabilities.remove("agentcapabilities.TestAgentWithListedCapabilities");
    }

    @Test
    public void shouldComputeCapabilitiesExactlyAsInAgentCapabilitiesJsonFile() {
        Map<String, Set<String>> expectedCapabilitiesMap =
                readExpectedAgentCapabilities(EXPECTED_AGENT_CAPABILITIES_FILE_PATH);
        capabilities.forEach(
                (agentName, computedCapabilities) -> {
                    try {
                        Set<String> expectedCapabilities = expectedCapabilitiesMap.get(agentName);

                        assertThat(getCapabilitiesAsSet(computedCapabilities))
                                .as("for agent %s capabilities", agentName)
                                .isEqualTo(expectedCapabilities);
                    } catch (AssertionError t) {
                        // get rid of useless stack trace
                        t.setStackTrace(new StackTraceElement[0]);
                        collector.addError(t);
                    }
                });
    }

    private Set<String> getCapabilitiesAsSet(Set<Capability> capabilities) {
        return capabilities.stream().map(Enum::name).collect(Collectors.toSet());
    }

    private Map<String, Set<String>> readExpectedAgentCapabilities(String filePath) {
        try {
            byte[] agentCapabilitiesFileData = Files.readAllBytes(Paths.get(filePath));
            Map<String, List<String>> capabilitiesMap =
                    new ObjectMapper().readValue(new String(agentCapabilitiesFileData), Map.class);
            return capabilitiesMap.entrySet().stream()
                    .collect(Collectors.toMap(Entry::getKey, convertFromListToSet));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
