package se.tink.backend.aggregation.agents.agentfactory;

import static java.util.stream.Collectors.groupingBy;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.BeanAccess;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.agents.rpc.ProviderStatuses;
import se.tink.backend.aggregation.agents.agentfactory.utils.AgentInitialisationUtil;
import se.tink.backend.aggregation.agents.agentfactory.utils.ProviderFetcherUtil;
import se.tink.backend.aggregation.agents.agentfactory.utils.TestConfigurationReaderUtil;

public class AgentInitialisationTest {

    private static AgentFactoryTestConfig agentFactoryTestConfig =
            new TestConfigurationReaderUtil(
                            "src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/agentfactory/resources/test_config.yml")
                    .getAgentFactoryTestConfig();

    private List<Provider> providerConfigurations =
            new ProviderFetcherUtil("external/tink_backend/src/provider_configuration/data/seeding")
                    .getProviderConfigurations();

    private static AgentFactoryTestConfig readTestConfiguration(String filePath)
            throws IOException {
        FileInputStream configFileStream = new FileInputStream(new File(filePath));
        Yaml yaml = new Yaml(new Constructor(AgentFactoryTestConfig.class));
        yaml.setBeanAccess(BeanAccess.FIELD);
        return yaml.loadAs(configFileStream, AgentFactoryTestConfig.class);
    }

    private void throwProperErrorMessageForAgentInitialisationException(
            Exception e, Provider provider) {
        String errorMessagePrefix =
                "Agent "
                        + provider.getClassName()
                        + " could not be instantiated for provider "
                        + provider.getName();
        if (e instanceof com.google.inject.ConfigurationException) {
            throw new RuntimeException(
                    errorMessagePrefix
                            + " probably due to missing guice dependency.\n"
                            + "Make sure that you add any additional modules to your agent via the "
                            + "@AgentDependencyModules annotation, and that these modules bind any "
                            + "dependency your agent may have.",
                    e);
        } else if (e instanceof ClassNotFoundException) {
            throw new RuntimeException(
                    errorMessagePrefix
                            + " due to ClassNotFound exception. \nPlease ensure the followings: \n"
                            + "1) Necessary runtime dep is included in src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/agentfactory/BUILD\n"
                            + "2) The className in provider configuration (which is in tink-backend) does not have any typo",
                    e);
        } else {
            throw new RuntimeException(errorMessagePrefix, e);
        }
    }

    // This method returns one provider for each agent
    private List<Provider> getProvidersForInitialisationTest() {
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
                .collect(Collectors.toList());
    }

    @Test
    public void whenEnabledProvidersAreGivenAgentFactoryShouldInstantiateAllEnabledAgents() {
        // given
        List<Provider> providers =
                getProvidersForInitialisationTest().stream()
                        .filter(
                                provider ->
                                        !agentFactoryTestConfig
                                                .getIgnoredAgentsForInitialisationTest()
                                                .contains(provider.getClassName()))
                        .collect(Collectors.toList());

        // given / when
        AgentInitialisationUtil util = new AgentInitialisationUtil("etc/test.yml");
        providers
                .parallelStream()
                .forEach(
                        provider -> {
                            try {
                                util.initialiseAgent(provider);
                            } catch (Exception e) {
                                throwProperErrorMessageForAgentInitialisationException(e, provider);
                            }
                        });

        /*
           What we want to test is to check whether we can initialise all agents without having
           an exception. For this reason, we don't have an explicit "then" block for this test
        */
    }
}
