package se.tink.backend.aggregation.agents.agentcapabilities;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.reflections.Reflections;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.client.provider_configuration.rpc.Capability;

public class AgentCapabilitiesService {

    private static final String DEFAULT_AGENT_PACKAGE_CLASS_PREFIX =
            "se.tink.backend.aggregation.agents";

    private final Reflections reflections = new Reflections(DEFAULT_AGENT_PACKAGE_CLASS_PREFIX);

    public Map<String, Set<Capability>> getAgentsCapabilities() {
        return reflections.getSubTypesOf(Agent.class).stream()
                .filter(hasCapabilities)
                .collect(Collectors.toMap(getAgentName, CapabilitiesExtractor::readCapabilities));
    }

    public Map<String, Map<String, Set<String>>> getAgentsPisCapabilities() {
        return reflections.getSubTypesOf(Agent.class).stream()
                .filter(hasPisCapabilities)
                .collect(
                        Collectors.toMap(getAgentName, CapabilitiesExtractor::readPisCapabilities));
    }

    private final Predicate<Class<? extends Agent>> hasCapabilities =
            klass -> klass.isAnnotationPresent(AgentCapabilities.class);

    private final Predicate<Class<? extends Agent>> hasPisCapabilities =
            klass -> klass.isAnnotationPresent(AgentPisCapability.class);

    private final Function<Class<? extends Agent>, String> getAgentName =
            klass -> klass.getName().replace(DEFAULT_AGENT_PACKAGE_CLASS_PREFIX + ".", "");
}
