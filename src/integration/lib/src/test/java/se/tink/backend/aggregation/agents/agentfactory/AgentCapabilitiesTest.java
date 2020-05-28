package se.tink.backend.aggregation.agents.agentfactory;

import static java.util.stream.Collectors.groupingBy;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import io.dropwizard.jackson.Jackson;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.agents.agentfactory.utils.AgentFactoryTestConfiguration;
import se.tink.backend.aggregation.agents.agentfactory.utils.AgentInitialisationUtil;
import se.tink.backend.aggregation.agents.agentfactory.utils.ProviderFetcherUtil;
import se.tink.backend.aggregation.agents.agentfactory.utils.TestConfigurationReaderUtil;

public class AgentCapabilitiesTest {

    private static final Logger log = LoggerFactory.getLogger(AgentCapabilitiesTest.class);

    private static final String TRANSFERS = "TRANSFERS";
    private static final String LOANS = "LOANS";
    private static final String MORTGAGE_AGGREGATION = "MORTGAGE_AGGREGATION";
    private static final String PAYMENTS = "PAYMENTS";

    private static final String TEST_CONFIGURATION_FILE_PATH = "etc/test.yml";
    private static final String CREDENTIALS_TEMPLATE_FILE_PATH =
            "src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/agentfactory/resources/credentials_template.json";
    private static final String USER_TEMPLATE_FILE_PATH =
            "src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/agentfactory/resources/user_template.json";

    private Map<String, List<String>> readExpectedAgentCapabilities(String filePath) {
        Map<String, List<String>> agentCapabilities;
        try {
            byte[] agentCapabilitiesFileData = Files.readAllBytes(Paths.get(filePath));
            agentCapabilities =
                    Jackson.newObjectMapper()
                            .readValue(new String(agentCapabilitiesFileData), Map.class);
            return agentCapabilities;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Set<String> collectGivenAgentCapabilities(
            Agent agent, Set<String> expectedCapabilities) {
        Set<String> givenCapabilities = new HashSet<>();
        if (agent instanceof RefreshCreditCardAccountsExecutor) {
            givenCapabilities.add("CREDIT_CARDS");
        }
        if (agent instanceof RefreshIdentityDataExecutor) {
            givenCapabilities.add("IDENTITY_DATA");
        }
        if (agent instanceof RefreshCheckingAccountsExecutor) {
            givenCapabilities.add("CHECKING_ACCOUNTS");
        }
        if (agent instanceof RefreshSavingsAccountsExecutor) {
            givenCapabilities.add("SAVINGS_ACCOUNTS");
        }
        if (agent instanceof RefreshInvestmentAccountsExecutor) {
            givenCapabilities.add("INVESTMENTS");
        }
        if (agent instanceof RefreshLoanAccountsExecutor) {
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
        if (agent instanceof TransferExecutor) {
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
        if (agent instanceof TransferExecutorNxgen) {
            if (expectedCapabilities.contains(TRANSFERS)) {
                givenCapabilities.add(TRANSFERS);
            }
            if (expectedCapabilities.contains(PAYMENTS)) {
                givenCapabilities.add(PAYMENTS);
            }
        }
        return givenCapabilities;
    }

    private Optional<String> compareExpectedAndGivenAgentCapabilities(
            Provider provider,
            AgentInitialisationUtil agentInitialisationUtil,
            Map<String, List<String>> expectedAgentCapabilities) {

        Agent agent;
        try {
            agent = agentInitialisationUtil.initialiseAgent(provider);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Skip because we cannot perform check for such agents
        if (agent instanceof DeprecatedRefreshExecutor) {
            return Optional.empty();
        }

        Set<String> expectedCapabilities =
                new HashSet<>(expectedAgentCapabilities.get(provider.getClassName()));

        Set<String> givenCapabilities = collectGivenAgentCapabilities(agent, expectedCapabilities);

        SetView<String> expectedButNotGiven =
                Sets.difference(new HashSet<>(expectedCapabilities), givenCapabilities);

        SetView<String> givenButNotExpected =
                Sets.difference(givenCapabilities, new HashSet<>(expectedCapabilities));

        StringBuilder builder = new StringBuilder();
        if (expectedButNotGiven.size() > 0) {
            builder.append(
                    "Agent "
                            + provider.getClassName()
                            + " has the following capabilities in agent-capabilities.json file, however it does not implement corresponding interface(s) for them : "
                            + expectedButNotGiven.toString()
                            + "\n");
        }

        if (givenButNotExpected.size() > 0) {
            builder.append(
                    "Agent "
                            + provider.getClassName()
                            + " has the following capabilities which are not mentioned in agent-capabilities.json : "
                            + givenButNotExpected.toString()
                            + "\n");
        }
        if (expectedButNotGiven.size() > 0 || givenButNotExpected.size() > 0) {
            return Optional.of(builder.toString());
        } else {
            return Optional.empty();
        }
    }

    // This method returns one provider for each agent
    private List<Provider> getProvidersForCapabilitiesTest(List<Provider> providerConfigurations) {
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

    /*
        For each agent (except the agents specified in resource/igore_agents_for_capability_test.txt)
        This test compares the real capabilities of the agent (by checking which interfaces it implements)
        and the expected capabilities of the agent (by checking agent-capabilities.json file in tink-backend)
        and fails if there is an agent where the real capabilities and expected capabilities are not
        matching.

        Known limitations:

        1- We do not make any assertions on PAYMENTS and TRANSFER capabilities for agents
           that implement TransferExecutorNxgen.
        2- We do not make any assertions on agents that implement DeprecatedRefreshExecutor
        3- We cannot perform tests on agents that are not tested for initialisation
    */
    @Test
    public void expectedCapabilitiesAndGivenCapabilitiesShouldMatchForUnignoredAgents() {
        // given
        Map<String, List<String>> expectedAgentCapabilities =
                readExpectedAgentCapabilities(
                        "external/tink_backend/src/provider_configuration/data/seeding/providers/capabilities/agent-capabilities.json");

        AgentFactoryTestConfiguration agentFactoryTestConfiguration =
                new TestConfigurationReaderUtil(
                                "src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/agentfactory/resources/test_config.yml")
                        .getAgentFactoryTestConfiguration();

        List<Provider> providerConfigurations =
                new ProviderFetcherUtil(
                                "external/tink_backend/src/provider_configuration/data/seeding")
                        .getProviderConfigurations();

        AgentInitialisationUtil agentInitialisationUtil =
                new AgentInitialisationUtil(
                        TEST_CONFIGURATION_FILE_PATH,
                        CREDENTIALS_TEMPLATE_FILE_PATH,
                        USER_TEMPLATE_FILE_PATH);

        List<Provider> providerForEachUnignoredAgent =
                getProvidersForCapabilitiesTest(providerConfigurations).stream()
                        .filter(
                                provider ->
                                        !agentFactoryTestConfiguration
                                                .getIgnoredAgentsForInitialisationTest()
                                                .contains(provider.getClassName()))
                        .filter(
                                provider ->
                                        !agentFactoryTestConfiguration
                                                .getIgnoredAgentsForCapabilityTest()
                                                .contains(provider.getClassName()))
                        .collect(Collectors.toList());

        // when
        List<String> errors =
                providerForEachUnignoredAgent
                        .parallelStream()
                        .map(
                                provider ->
                                        compareExpectedAndGivenAgentCapabilities(
                                                provider,
                                                agentInitialisationUtil,
                                                expectedAgentCapabilities))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList());

        errors.stream().forEach(log::error);

        // then
        assertEquals(0, errors.size());
    }
}
