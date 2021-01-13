package se.tink.backend.aggregation.resources.agentcapabilities;

import com.google.inject.Inject;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilitiesService;
import se.tink.backend.aggregation.api.AgentCapabilitiesResource;
import se.tink.backend.aggregation.client.provider_configuration.rpc.Capability;

public class AgentCapabilitiesResourceImpl implements AgentCapabilitiesResource {

    private static final Function<Entry<String, Set<Capability>>, Set<String>>
            capabilitiesAsStringSet =
                    entry -> entry.getValue().stream().map(Enum::name).collect(Collectors.toSet());

    private AgentCapabilitiesService agentCapabilitiesService;

    @Inject
    public AgentCapabilitiesResourceImpl(AgentCapabilitiesService agentCapabilitiesService) {
        this.agentCapabilitiesService = agentCapabilitiesService;
    }

    @Override
    public Map<String, Set<String>> getAgentCapabilities() {
        return agentCapabilitiesService.getAgentsCapabilities().entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, capabilitiesAsStringSet));
    }

    @Override
    public Map<String, Map<String, Set<String>>> getAgentPisCapabilities() {
        return agentCapabilitiesService.getAgentsPisCapabilities();
    }
}
