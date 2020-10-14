package se.tink.backend.aggregation.agents.agentcapabilities;

import static java.util.stream.Collectors.groupingBy;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.agents.rpc.ProviderStatuses;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.agents.agentfactory.utils.AgentFactoryTestConfiguration;
import se.tink.backend.aggregation.agents.agentfactory.utils.ProviderReader;
import se.tink.backend.aggregation.agents.agentfactory.utils.TestConfigurationReader;
import se.tink.backend.aggregation.client.provider_configuration.rpc.Capability;

public class AgentCapabilitiesServiceTest {

    private static final String DEFAULT_AGENT_PACKAGE_CLASS_PREFIX =
            "se.tink.backend.aggregation.agents";

    private static final String IGNORED_AGENTS_FOR_TESTS_FILE_PATH =
            "src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/agentfactory/resources/ignored_agents_for_tests.yml";

    private static final String PROVIDER_CONFIG_FOLDER_PATH =
            "external/tink_backend/src/provider_configuration/data/seeding";

    private static final AgentFactoryTestConfiguration agentFactoryTestConfiguration =
            new TestConfigurationReader().read(IGNORED_AGENTS_FOR_TESTS_FILE_PATH);

    @Rule public ErrorCollector collector = new ErrorCollector();

    private Map<String, Set<Capability>> capabilities;
    private Set<Provider> activeProviders;

    @Before
    public void init() {
        capabilities = new AgentCapabilitiesService().getAgentsCapabilities();
        activeProviders =
                getActiveProviders(
                        new ProviderReader()
                                .getProviderConfigurations(PROVIDER_CONFIG_FOLDER_PATH));
    }

    @Test
    public void shouldHaveCapabilitiesForActiveProviders() {
        Set<String> providersAgents =
                activeProviders.stream().map(Provider::getClassName).collect(Collectors.toSet());
        Set<String> agentsWithCapabilitiesAnnotation =
                capabilities.entrySet().stream().map(Entry::getKey).collect(Collectors.toSet());

        providersAgents.removeAll(agentsWithCapabilitiesAnnotation);
        assertThat(providersAgents)
                .as("missing @AgentCapabilities annotation for agents:{}", providersAgents)
                .isEmpty();
    }

    @Test
    public void agentShouldImplementInterfacesForExpectedCapabilities() {
        getFilterAgents()
                .forEach(
                        (agentName, expectedCapabilities) -> {
                            Class<? extends Agent> agentClass;
                            try {
                                agentClass = getAgentClass(agentName);
                            } catch (ClassNotFoundException e) {
                                collector.addError(e);
                                return;
                            }
                            for (Capability capability : expectedCapabilities) {
                                try {
                                    Set<Mapping> mapping = getMappingsFor(capability);
                                    Assert.assertTrue(
                                            "agent :"
                                                    + agentName
                                                    + " is marked with annotation @AgentCapability as capable of: "
                                                    + capability
                                                    + " but doesnt implement any of: "
                                                    + mapping.stream()
                                                            .map(Mapping::getExecutorClass)
                                                            .collect(Collectors.toSet()),
                                            mapping.stream().anyMatch(m -> m.canMap(agentClass)));
                                } catch (AssertionError t) {
                                    // get rid of useless stack trace
                                    t.setStackTrace(new StackTraceElement[0]);
                                    collector.addError(t);
                                }
                            }
                        });
    }

    private Map<String, Set<Capability>> getFilterAgents() {
        Map<String, Set<Capability>> filteredCapabilities = new HashMap<>(capabilities);

        agentFactoryTestConfiguration
                .getIgnoredAgentsForCapabilityTest()
                .forEach(filteredCapabilities::remove);

        return filteredCapabilities;
    }

    private Set<Mapping> getMappingsFor(Capability capability) {
        Set<Mapping> mapping = CapabilitiesExtractor.getMappingsFor(capability);
        // handle deprecated capabilities
        if (capability.equals(Capability.MORTGAGE_AGGREGATION)) {
            mapping.addAll(CapabilitiesExtractor.getMappingsFor(Capability.LOANS));
        } else if (capability.equals(Capability.TRANSFERS)) {
            mapping.addAll(CapabilitiesExtractor.getMappingsFor(Capability.PAYMENTS));
        }
        return mapping;
    }

    private Class<? extends Agent> getAgentClass(String agentName) throws ClassNotFoundException {
        return (Class<? extends Agent>)
                Class.forName(DEFAULT_AGENT_PACKAGE_CLASS_PREFIX + "." + agentName);
    }

    // This method returns one provider for each agent (for each agent it picks the provider
    // which is the first in alphabetical order)
    private Set<Provider> getActiveProviders(Set<Provider> providerConfigurations) {
        return providerConfigurations.stream()
                .filter(
                        provider ->
                                ProviderStatuses.ENABLED.equals(provider.getStatus())
                                        || ProviderStatuses.OBSOLETE.equals(provider.getStatus()))
                .filter(provider -> !provider.getName().toLowerCase().contains("test"))
                .collect(groupingBy(Provider::getClassName))
                .entrySet()
                .stream()
                .map(
                        entry -> {
                            entry.getValue().sort(Comparator.comparing(Provider::getName));
                            return entry.getValue().get(0);
                        })
                .collect(Collectors.toSet());
    }
}
