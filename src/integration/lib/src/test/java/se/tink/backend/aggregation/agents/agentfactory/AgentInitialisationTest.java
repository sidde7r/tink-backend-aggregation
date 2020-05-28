package se.tink.backend.aggregation.agents.agentfactory;

import static java.util.stream.Collectors.groupingBy;
import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.agents.rpc.ProviderStatuses;
import se.tink.backend.aggregation.agents.agentfactory.utils.AgentFactoryTestConfiguration;
import se.tink.backend.aggregation.agents.agentfactory.utils.AgentInitialisor;
import se.tink.backend.aggregation.agents.agentfactory.utils.ProviderFetcher;
import se.tink.backend.aggregation.agents.agentfactory.utils.TestConfigurationReader;

public class AgentInitialisationTest {

    private static final Logger log = LoggerFactory.getLogger(AgentInitialisationTest.class);

    private static final String TEST_CONFIGURATION_FILE_PATH = "etc/test.yml";
    private static final String CREDENTIALS_TEMPLATE_FILE_PATH =
            "src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/agentfactory/resources/credentials_template.json";
    private static final String USER_TEMPLATE_FILE_PATH =
            "src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/agentfactory/resources/user_template.json";

    private String createProperErrorMessageForAgentInitialisationError(
            Exception e, Provider provider) {
        String errorMessagePrefix =
                "Agent "
                        + provider.getClassName()
                        + " could not be instantiated for provider "
                        + provider.getName();
        if (e instanceof com.google.inject.ConfigurationException) {
            return errorMessagePrefix
                    + " probably due to missing guice dependency.\n"
                    + "Make sure that you add any additional modules to your agent via the "
                    + "@AgentDependencyModules annotation, and that these modules bind any "
                    + "dependency your agent may have.\n"
                    + e.toString();
        } else if (e instanceof ClassNotFoundException) {
            return errorMessagePrefix
                    + " due to ClassNotFound exception. \nPlease ensure the followings: \n"
                    + "1) Necessary runtime dep is included in src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/agentfactory/BUILD\n"
                    + "2) The className in provider configuration (which is in tink-backend) does not have any typo\n"
                    + e.toString();
        } else {
            return errorMessagePrefix + "\n" + e.toString();
        }
    }

    // This method returns one provider for each agent
    private Set<Provider> getProvidersForInitialisationTest(List<Provider> providerConfigurations) {
        return providerConfigurations.stream()
                .filter(
                        provider ->
                                ProviderStatuses.ENABLED.equals(provider.getStatus())
                                        || ProviderStatuses.OBSOLETE.equals(provider.getStatus()))
                .filter(provider -> !provider.getName().toLowerCase().contains("test"))
                .collect(groupingBy(Provider::getClassName))
                .entrySet()
                .stream()
                .map(entry -> entry.getValue().get(0))
                .collect(Collectors.toSet());
    }

    /*
       For each agent (except the agents specified in resources/ignored_agents_for_tests.yml)
       this test checks whether we can initialise the agent or not.
    */
    @Test
    public void whenEnabledProvidersAreGivenAgentFactoryShouldInstantiateAllEnabledAgents() {
        // given
        AgentFactoryTestConfiguration agentFactoryTestConfiguration =
                new TestConfigurationReader(
                                "src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/agentfactory/resources/ignored_agents_for_tests.yml")
                        .getAgentFactoryTestConfiguration();

        List<Provider> providerConfigurations =
                new ProviderFetcher(
                                "external/tink_backend/src/provider_configuration/data/seeding")
                        .getProviderConfigurations();

        AgentInitialisor agentInitialisor =
                new AgentInitialisor(
                        TEST_CONFIGURATION_FILE_PATH,
                        CREDENTIALS_TEMPLATE_FILE_PATH,
                        USER_TEMPLATE_FILE_PATH);

        Set<Provider> providers =
                getProvidersForInitialisationTest(providerConfigurations).stream()
                        .filter(
                                provider ->
                                        !agentFactoryTestConfiguration
                                                .getIgnoredAgentsForInitialisationTest()
                                                .contains(provider.getClassName()))
                        .collect(Collectors.toSet());

        // when
        Set<String> errors = new HashSet<>();
        providers
                .parallelStream()
                .forEach(
                        provider -> {
                            try {
                                agentInitialisor.initialiseAgent(provider);
                            } catch (Exception e) {
                                errors.add(
                                        createProperErrorMessageForAgentInitialisationError(
                                                e, provider));
                            }
                        });

        errors.stream().forEach(log::error);

        // then
        assertEquals(0, errors.size());
    }
}
