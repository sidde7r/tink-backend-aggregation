package se.tink.backend.aggregation.agents.agentfactory;

import static java.util.stream.Collectors.groupingBy;

import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import io.dropwizard.jackson.Jackson;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Test;
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
import se.tink.backend.aggregation.agents.agentfactory.utils.AgentInitialisationUtil;
import se.tink.backend.aggregation.agents.agentfactory.utils.ProviderFetcherUtil;
import se.tink.backend.aggregation.agents.agentfactory.utils.TestConfigurationReaderUtil;

public class AgentCapabilitiesTest {

    /*
       Map from agent class name to list of expected capabilities
       (we read this from agent-capabilities.json from tink-backend)
    */
    private static Map<String, List<String>> expectedAgentCapabilities =
            readExpectedAgentCapabilities(
                    "external/tink_backend/src/provider_configuration/data/seeding/providers/capabilities/agent-capabilities.json");

    private static AgentFactoryTestConfig agentFactoryTestConfig =
            new TestConfigurationReaderUtil(
                            "src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/agentfactory/resources/test_config.yml")
                    .getAgentFactoryTestConfig();

    private List<Provider> providerConfigurations =
            new ProviderFetcherUtil("external/tink_backend/src/provider_configuration/data/seeding")
                    .getProviderConfigurations();

    private static Map<String, List<String>> readExpectedAgentCapabilities(String filePath) {
        // given
        Path path = Paths.get(filePath);

        Map<String, List<String>> agentCapabilities;
        try {
            byte[] agentCapabilitiesFileData = Files.readAllBytes(path);
            agentCapabilities =
                    Jackson.newObjectMapper()
                            .readValue(new String(agentCapabilitiesFileData), Map.class);
            return agentCapabilities;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void compareExpectedAndGivenAgentCapabilities(Provider provider) throws Exception {

        Agent agent = new AgentInitialisationUtil("etc/test.yml").initialiseAgent(provider);

        // Skip capability checks because we cannot do that for these agents
        if (agent instanceof DeprecatedRefreshExecutor) {
            return;
        }

        // given
        // Find given and expected agent capabilitie
        Set<String> givenCapabilities = new HashSet<>();
        List<String> expectedCapabilities = expectedAgentCapabilities.get(provider.getClassName());

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
            if (expectedCapabilities.contains("LOANS")) {
                givenCapabilities.add("LOANS");
                relatedGivenCapability = true;
            }
            if (expectedCapabilities.contains("MORTGAGE_AGGREGATION")) {
                givenCapabilities.add("MORTGAGE_AGGREGATION");
                relatedGivenCapability = true;
            }
            if (!relatedGivenCapability) {
                // Not MORTGAGE_AGGREGATION because LOANS is the new capability that covers
                // all, MORTGAGE_AGGREGATION is just there for backward compatibility
                givenCapabilities.add("LOANS");
            }
        }
        if (agent instanceof TransferExecutor) {
            boolean relatedGivenCapability = false;
            if (expectedCapabilities.contains("TRANSFERS")) {
                givenCapabilities.add("TRANSFERS");
                relatedGivenCapability = true;
            }
            if (expectedCapabilities.contains("PAYMENTS")) {
                givenCapabilities.add("PAYMENTS");
                relatedGivenCapability = true;
            }
            if (!relatedGivenCapability) {
                // Not TRANSFERS because PAYMENTS and TRANSFERS are the same and PAYMENTS is
                // newer
                // TRANSFER is there just for backward compatibility
                givenCapabilities.add("PAYMENTS");
            }
        }
        /*
        If agent is TransferExecutorNxgen, there is no way for us to determine if this agent
        has transfer/payments capability or not so we will not make any assertions on that

        Turned out that an agent implementing TransferExecutorNxgen interface does not prove
        that it should have the TRANSFER capability (see AxaAgent)
         */
        if (agent instanceof TransferExecutorNxgen) {
            if (expectedCapabilities.contains("TRANSFERS")) {
                expectedCapabilities.remove("TRANSFERS");
            }
            if (expectedCapabilities.contains("PAYMENTS")) {
                expectedCapabilities.remove("PAYMENTS");
            }
        }

        // then
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
            throw new RuntimeException(builder.toString());
        }
    }

    // This method returns one provider for each agent
    private List<Provider> getProvidersForCapabilitiesTest() {
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

        1- We do not make any assertions on PAYMENTS and TRANSFER capabilities.
        2- We do not make any assertions on agents that implement DeprecatedRefreshExecutor
        3- We cannot perform tests on agents that are not tested for initialisation
    */
    @Test
    public void expectedCapabilitiesAndGivenCapabilitiesShouldMatchForAllAgents() {
        // given
        List<Provider> providers =
                getProvidersForCapabilitiesTest().stream()
                        .filter(
                                provider ->
                                        !agentFactoryTestConfig
                                                .getIgnoredAgentsForInitialisationTest()
                                                .contains(provider.getClassName()))
                        .filter(
                                provider ->
                                        !agentFactoryTestConfig
                                                .getIgnoredAgentsForCapabilityTest()
                                                .contains(provider.getClassName()))
                        .collect(Collectors.toList());

        // when / then
        providers
                .parallelStream()
                .forEach(
                        provider -> {
                            try {
                                this.compareExpectedAndGivenAgentCapabilities(provider);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        });
    }
}
