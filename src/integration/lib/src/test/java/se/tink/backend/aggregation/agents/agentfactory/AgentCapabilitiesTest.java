package se.tink.backend.aggregation.agents.agentfactory;

import static java.util.stream.Collectors.groupingBy;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import io.dropwizard.jackson.Jackson;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.agents.rpc.ProviderStatuses;
import se.tink.backend.aggregation.agents.DeprecatedRefreshExecutor;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.RefreshInvestmentAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshLoanAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.TransferExecutor;
import se.tink.backend.aggregation.agents.TransferExecutorNxgen;
import se.tink.backend.aggregation.agents.agentfactory.utils.AgentFactoryTestConfiguration;
import se.tink.backend.aggregation.agents.agentfactory.utils.ProviderReader;
import se.tink.backend.aggregation.agents.agentfactory.utils.TestConfigurationReader;
import se.tink.libraries.pair.Pair;

/*
    For each agent (except the agents specified in resources/ignored_agents_for_tests.yml)
    These tests compares the real capabilities of the agent (by checking which interfaces it implements)
    and the expected capabilities of the agent (by checking agent-capabilities.json file in tink-backend)
    and fails if there is an agent where the real capabilities and expected capabilities are not
    matching.

    Limitations:

    1- We do not make any assertions on PAYMENTS and TRANSFER capabilities for agents
       that implement TransferExecutorNxgen.
    2- We do not make any assertions on agents that implement DeprecatedRefreshExecutor
*/
public class AgentCapabilitiesTest {
    private static final Logger log = LoggerFactory.getLogger(AgentCapabilitiesTest.class);

    private static final String TRANSFERS = "TRANSFERS";
    private static final String LOANS = "LOANS";
    private static final String MORTGAGE_AGGREGATION = "MORTGAGE_AGGREGATION";
    private static final String PAYMENTS = "PAYMENTS";

    private static final String IGNORED_AGENTS_FOR_TESTS_FILE_PATH =
            "src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/agentfactory/resources/ignored_agents_for_tests.yml";

    private static final String EXPECTED_AGENT_CAPABILITIES_FILE_PATH =
            "external/tink_backend/src/provider_configuration/data/seeding/providers/capabilities/agent-capabilities.json";

    private static final String PROVIDER_CONFIG_FOLDER_PATH =
            "external/tink_backend/src/provider_configuration/data/seeding";

    private static final Map<String, Set<String>> expectedAgentCapabilities =
            readExpectedAgentCapabilities(EXPECTED_AGENT_CAPABILITIES_FILE_PATH);

    private static final AgentFactoryTestConfiguration agentFactoryTestConfiguration =
            new TestConfigurationReader().read(IGNORED_AGENTS_FOR_TESTS_FILE_PATH);

    private static final Set<Provider> providerConfigurations =
            new ProviderReader().getProviderConfigurations(PROVIDER_CONFIG_FOLDER_PATH);

    private static Map<String, Set<String>> readExpectedAgentCapabilities(String filePath) {
        Map<String, Set<String>> agentCapabilities;
        try {
            byte[] agentCapabilitiesFileData = Files.readAllBytes(Paths.get(filePath));
            agentCapabilities =
                    Jackson.newObjectMapper()
                            .readValue(new String(agentCapabilitiesFileData), Map.class);
            return agentCapabilities;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Set<String> collectGivenAgentCapabilities(
            Class<?> agentClass, Set<String> expectedCapabilities) {
        Set<String> givenCapabilities = new HashSet<>();
        if (RefreshCreditCardAccountsExecutor.class.isAssignableFrom(agentClass)) {
            givenCapabilities.add("CREDIT_CARDS");
        }
        if (RefreshIdentityDataExecutor.class.isAssignableFrom(agentClass)) {
            givenCapabilities.add("IDENTITY_DATA");
        }
        if (RefreshCheckingAccountsExecutor.class.isAssignableFrom(agentClass)) {
            givenCapabilities.add("CHECKING_ACCOUNTS");
        }
        if (RefreshSavingsAccountsExecutor.class.isAssignableFrom(agentClass)) {
            givenCapabilities.add("SAVINGS_ACCOUNTS");
        }
        if (RefreshInvestmentAccountsExecutor.class.isAssignableFrom(agentClass)) {
            givenCapabilities.add("INVESTMENTS");
        }
        if (RefreshLoanAccountsExecutor.class.isAssignableFrom(agentClass)) {
            boolean relatedGivenCapability = false;
            if (expectedCapabilities.contains(LOANS)) {
                givenCapabilities.add(LOANS);
                relatedGivenCapability = true;
            }
            if (expectedCapabilities.contains(MORTGAGE_AGGREGATION)) {
                givenCapabilities.add(MORTGAGE_AGGREGATION);
                relatedGivenCapability = true;
            }
            if (!relatedGivenCapability) {
                // Not MORTGAGE_AGGREGATION because LOANS is the new capability that covers
                // all, MORTGAGE_AGGREGATION is just there for backward compatibility
                givenCapabilities.add(LOANS);
            }
        }
        if (TransferExecutor.class.isAssignableFrom(agentClass)) {
            boolean relatedGivenCapability = false;
            if (expectedCapabilities.contains(TRANSFERS)) {
                givenCapabilities.add(TRANSFERS);
                relatedGivenCapability = true;
            }
            if (expectedCapabilities.contains(PAYMENTS)) {
                givenCapabilities.add(PAYMENTS);
                relatedGivenCapability = true;
            }
            if (!relatedGivenCapability) {
                // Not TRANSFERS because PAYMENTS and TRANSFERS are the same and PAYMENTS is
                // newer
                // TRANSFER is there just for backward compatibility
                givenCapabilities.add(PAYMENTS);
            }
        }
        /*
        If agent is TransferExecutorNxgen, there is no way for us to determine if this agent
        has transfer/payments capability or not so we will not make any assertions on that

        Turned out that an agent implementing TransferExecutorNxgen interface does not prove
        that it should have the TRANSFER capability (see AxaAgent)
         */
        if (TransferExecutorNxgen.class.isAssignableFrom(agentClass)) {
            if (expectedCapabilities.contains(TRANSFERS)) {
                givenCapabilities.add(TRANSFERS);
            }
            if (expectedCapabilities.contains(PAYMENTS)) {
                givenCapabilities.add(PAYMENTS);
            }
        }
        return givenCapabilities;
    }

    private Class<?> getClassForProvider(Provider provider) {
        try {
            return AgentClassFactory.getAgentClass(provider);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private SetView<String> getGivenButNotExpectedCapabilities(
            Provider provider, Map<String, Set<String>> expectedAgentCapabilities) {

        Class<?> agentClass = getClassForProvider(provider);

        Set<String> expectedCapabilities =
                new HashSet<>(expectedAgentCapabilities.get(provider.getClassName()));

        Set<String> givenCapabilities =
                collectGivenAgentCapabilities(agentClass, expectedCapabilities);

        return Sets.difference(givenCapabilities, new HashSet<>(expectedCapabilities));
    }

    private SetView<String> getExpectedButNotGivenCapabilities(
            Provider provider, Map<String, Set<String>> expectedAgentCapabilities) {

        Class<?> agentClass = getClassForProvider(provider);

        Set<String> expectedCapabilities =
                new HashSet<>(expectedAgentCapabilities.get(provider.getClassName()));

        Set<String> givenCapabilities =
                collectGivenAgentCapabilities(agentClass, expectedCapabilities);

        return Sets.difference(expectedCapabilities, new HashSet<>(givenCapabilities));
    }

    // This method returns one provider for each agent (for each agent it picks the provider
    // which is the first in alphabetical order)
    private Set<Provider> getProvidersForCapabilitiesTest(Set<Provider> providerConfigurations) {
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

    @Test
    public void everyAgentShouldImplementCapabilitiesListedInTheAgentCapabilitiesJsonFile() {
        // given
        Set<Provider> providersForUnignoredAgents =
                getProvidersForCapabilitiesTest(providerConfigurations).stream()
                        .filter(
                                provider ->
                                        !agentFactoryTestConfiguration
                                                .getIgnoredAgentsForCapabilityTest()
                                                .contains(provider.getClassName()))
                        .filter(
                                provider ->
                                        !DeprecatedRefreshExecutor.class.isAssignableFrom(
                                                getClassForProvider(provider)))
                        .collect(Collectors.toSet());

        // when
        Set<Pair<Provider, SetView<String>>> expectedButNotGivenCapabilities =
                providersForUnignoredAgents
                        .parallelStream()
                        .map(
                                provider ->
                                        new Pair<>(
                                                provider,
                                                getExpectedButNotGivenCapabilities(
                                                        provider, expectedAgentCapabilities)))
                        .collect(Collectors.toSet());

        // then
        expectedButNotGivenCapabilities.stream()
                .filter(diff -> diff.second.size() > 0)
                .forEach(
                        diff ->
                                log.error(
                                        "Agent "
                                                + diff.first.getClassName()
                                                + " has the following capabilities in agent-capabilities.json file, however it does not implement corresponding interface(s) for them : "
                                                + diff.second.toString()
                                                + "\n If this is intentional, please go to ignored_agents_for_tests.yaml file and add your agent into the ignored agents list"));

        assertEquals(
                0,
                expectedButNotGivenCapabilities.stream()
                        .filter(diff -> diff.second.size() > 0)
                        .collect(Collectors.toSet())
                        .size());
    }

    @Test
    public void everyCapabilityListedInTheAgentCapabilitiesJsonFileShouldBeImplementedByTheAgent() {
        // given
        Set<Provider> providersForUnignoredAgents =
                getProvidersForCapabilitiesTest(providerConfigurations).stream()
                        .filter(
                                provider ->
                                        !agentFactoryTestConfiguration
                                                .getIgnoredAgentsForCapabilityTest()
                                                .contains(provider.getClassName()))
                        .filter(
                                provider ->
                                        !DeprecatedRefreshExecutor.class.isAssignableFrom(
                                                getClassForProvider(provider)))
                        .collect(Collectors.toSet());

        // when
        Set<Pair<Provider, SetView<String>>> givenButNotExpectedCapabilities =
                providersForUnignoredAgents.stream()
                        .map(
                                provider ->
                                        new Pair<>(
                                                provider,
                                                getGivenButNotExpectedCapabilities(
                                                        provider, expectedAgentCapabilities)))
                        .collect(Collectors.toSet());

        // then
        givenButNotExpectedCapabilities.stream()
                .filter(diff -> diff.second.size() > 0)
                .forEach(
                        diff ->
                                log.error(
                                        "Agent "
                                                + diff.first.getClassName()
                                                + " has the following capabilities which are not mentioned in agent-capabilities.json : "
                                                + diff.second.toString()
                                                + "\n If this is intentional, please go to ignored_agents_for_tests.yaml file and add your agent into the ignored agents list"));

        assertEquals(
                0,
                givenButNotExpectedCapabilities.stream()
                        .filter(diff -> diff.second.size() > 0)
                        .collect(Collectors.toSet())
                        .size());
    }
}
