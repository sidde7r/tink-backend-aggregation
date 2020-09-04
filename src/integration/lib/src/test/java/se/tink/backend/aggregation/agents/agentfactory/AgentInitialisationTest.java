package se.tink.backend.aggregation.agents.agentfactory;

import static java.util.stream.Collectors.groupingBy;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.Assert.assertNull;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.agents.rpc.ProviderStatuses;
import se.tink.backend.aggregation.agents.agentfactory.utils.AgentFactoryTestConfiguration;
import se.tink.backend.aggregation.agents.agentfactory.utils.AgentInitialisor;
import se.tink.backend.aggregation.agents.agentfactory.utils.ProviderReader;
import se.tink.backend.aggregation.agents.agentfactory.utils.TestConfigurationReader;

public class AgentInitialisationTest {

    private static final Logger log = LoggerFactory.getLogger(AgentInitialisationTest.class);

    private static final String TEST_CONFIGURATION_FILE_PATH = "etc/test.yml";
    private static final String CREDENTIALS_TEMPLATE_FILE_PATH =
            "src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/agentfactory/resources/credentials_template.json";
    private static final String USER_TEMPLATE_FILE_PATH =
            "src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/agentfactory/resources/user_template.json";
    private static final String IGNORED_AGENTS_FOR_TESTS_FILE_PATH =
            "src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/agentfactory/resources/ignored_agents_for_tests.yml";

    private Throwable getRootCause(Throwable throwable) {
        Throwable rootCause = new Throwable(throwable);
        while (rootCause != null) {
            if (rootCause instanceof com.google.inject.ConfigurationException
                    || rootCause instanceof ClassNotFoundException) {
                return rootCause;
            }
            rootCause = rootCause.getCause();
        }
        return throwable;
    }

    private String getStackTrace(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }

    private String createProperErrorMessageForAgentInitialisationError(Throwable e) {
        String errorMessagePrefix = "Agent could not be instantiated";

        Throwable rootCause = getRootCause(e);
        if (rootCause instanceof com.google.inject.ConfigurationException) {
            return errorMessagePrefix
                    + " probably due to missing guice dependency.\n"
                    + "Make sure that you add any additional modules to your agent via the "
                    + "@AgentDependencyModules annotation, and that these modules bind any "
                    + "dependency your agent may have.\n"
                    + getStackTrace(e);
        } else if (rootCause instanceof ClassNotFoundException) {
            return errorMessagePrefix
                    + " due to ClassNotFound exception. \nPlease ensure the followings: \n"
                    + "1) Necessary runtime dep is included in src/integration/lib/src/main/java/se/tink/backend/aggregation/agents/agentfactory/BUILD\n"
                    + "2) The className in provider configuration (which is in tink-backend) does not have any typo\n"
                    + getStackTrace(e);
        } else {
            return errorMessagePrefix
                    + "\nOne reason might be that the proper dummy secrets are not provided. "
                    + "To ensure that this is not the case, please check test.yml and check that all "
                    + "proper dummy secrets are included for your agent. \nYou can also execute dummy_secrets_adder.py "
                    + "to add dummy secrets to this file. The following is the error message coming from the agent:\n"
                    + getStackTrace(e);
        }
    }

    // This method returns one provider for each agent
    private Set<Provider> getProvidersForInitialisationTest(Set<Provider> providerConfigurations) {
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
                            entry.getValue().sort((p1, p2) -> p1.getName().compareTo(p2.getName()));
                            return entry.getValue().get(0);
                        })
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
                new TestConfigurationReader().read(IGNORED_AGENTS_FOR_TESTS_FILE_PATH);

        Set<Provider> providerConfigurations =
                new ProviderReader()
                        .getProviderConfigurations(
                                "external/tink_backend/src/provider_configuration/data/seeding");

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
        Throwable throwable =
                catchThrowable(
                        () ->
                                providers.stream()
                                        .forEach(
                                                provider -> {
                                                    agentInitialisor.initialiseAgent(provider);
                                                }));

        // then
        if (throwable != null) {
            log.error(createProperErrorMessageForAgentInitialisationError(throwable));
        }
        assertNull(throwable);
    }
}
