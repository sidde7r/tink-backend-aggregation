package se.tink.backend.aggregation.agents.agentfactory.initialisation;

import static java.util.stream.Collectors.groupingBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import io.dropwizard.configuration.ConfigurationException;
import io.dropwizard.configuration.ConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.setup.Environment;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.validation.Validation;
import javax.validation.Validator;
import org.junit.BeforeClass;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.BeanAccess;
import se.tink.backend.agents.rpc.Credentials;
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
import se.tink.backend.aggregation.agents.agentfactory.iface.AgentFactory;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.aggregationcontroller.v1.core.HostConfiguration;
import se.tink.backend.aggregation.api.AggregatorInfo;
import se.tink.backend.aggregation.configuration.ProviderConfig;
import se.tink.backend.aggregation.configuration.guice.modules.AggregationModuleFactory;
import se.tink.backend.aggregation.configuration.guice.modules.FakeCryptoConfigurationsRepository;
import se.tink.backend.aggregation.configuration.models.AggregationServiceConfiguration;
import se.tink.backend.aggregation.fakelogmasker.FakeLogMasker;
import se.tink.backend.aggregation.nxgen.controllers.configuration.AgentConfigurationController;
import se.tink.backend.aggregation.nxgen.controllers.configuration.iface.AgentConfigurationControllerable;
import se.tink.backend.aggregation.storage.database.models.AggregatorConfiguration;
import se.tink.backend.aggregation.storage.database.models.ClientConfiguration;
import se.tink.backend.aggregation.storage.database.models.ClusterConfiguration;
import se.tink.backend.aggregation.storage.database.models.CryptoConfiguration;
import se.tink.backend.aggregation.storage.database.repositories.CryptoConfigurationsRepository;
import se.tink.backend.integration.tpp_secrets_service.client.iface.TppSecretsServiceClient;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.ManualAuthenticateRequest;
import se.tink.libraries.metrics.registry.MetricRegistry;
import se.tink.libraries.user.rpc.User;

public class AgentInitialisationTest {

    private static final Validator VALIDATOR =
            Validation.buildDefaultValidatorFactory().getValidator();

    private static final ObjectMapper MAPPER = Jackson.newObjectMapper();

    private static final Environment ENVIRONMENT =
            new Environment(
                    "test",
                    MAPPER,
                    VALIDATOR,
                    new com.codahale.metrics.MetricRegistry(),
                    ClassLoader.getSystemClassLoader());

    private static final String TEST_CLUSTER_ID = "testCluster-DummyId";

    private static final String CREDENTIALS_OBJECT_TEMPLATE =
            "{\"fields\":{\"username\":\"username\"},\"debugUntil\":695260800,\"providerLatency\":0,\"id\":\"refresh-test\",\"nextUpdate\":null,\"payload\":\"\",\"providerName\":\"%s\",\"sessionExpiryDate\":null,\"status\":\"CREATED\",\"statusPayload\":\"CREATED\",\"statusPrompt\":\"\",\"statusUpdated\":\"695260800\",\"supplementalInformation\":\"\",\"type\":\"PASSWORD\",\"updated\":\"695260800\",\"userId\":\"userId\",\"dataVersion\":1}";

    private static final String USER_OBJECT_TEMPLATE =
            "{\"flags\": [], \"flagsSerialized\": \"[]\", \"id\": \"userId\", \"profile\": {\"locale\": \"en_US\"}, \"username\": \"username\", \"debugUntil\": 695260800 }";

    private static User user;
    private static AggregationServiceConfiguration configuration;
    private static Map<String, List<String>> agentCapabilities;
    private static List<Provider> providerConfigurationsForEnabledProviders;
    private static Set<Module> guiceModulesToUse;
    private static HostConfiguration hostConfiguration;
    private static Injector injector;
    private static AgentFactory agentFactory;
    private static AgentInitialisationTestConfig agentInitialisationTestConfig;

    private static Map<String, List<String>> readCapabilities(String filePath) {
        // given
        Path path = Paths.get(filePath);

        Map<String, List<String>> agentCapabilities;
        try {
            byte[] agentCapabilitiesFileData = Files.readAllBytes(path);
            agentCapabilities =
                    new ObjectMapper().readValue(new String(agentCapabilitiesFileData), Map.class);
            return agentCapabilities;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static AgentInitialisationTestConfig readTestConfiguration(String filePath)
            throws IOException {
        FileInputStream configFileStream = new FileInputStream(new File(filePath));
        Yaml yaml = new Yaml(new Constructor(AgentInitialisationTestConfig.class));
        yaml.setBeanAccess(BeanAccess.FIELD);
        return yaml.loadAs(configFileStream, AgentInitialisationTestConfig.class);
    }

    private static AggregationServiceConfiguration readConfiguration(String filePath)
            throws IOException, ConfigurationException {

        ConfigurationFactory<AggregationServiceConfiguration> configurationFactory =
                new ConfigurationFactory<>(
                        AggregationServiceConfiguration.class, VALIDATOR, MAPPER, "");

        return configurationFactory.build(new File(filePath));
    }

    private static ProviderConfig readProviderConfiguration(File file) {
        try {
            return new ObjectMapper().readValue(file, ProviderConfig.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<File> getProviderConfigurationFiles(String folderForConfigurations) {
        return Arrays.asList(new File(folderForConfigurations).listFiles()).stream()
                .filter(file -> file.getName().contains("providers-"))
                .filter(file -> !file.getName().contains("development"))
                .collect(Collectors.toList());
    }

    private static Stream<Provider> mapProviders(ProviderConfig config) {
        return config.getProviders().stream()
                .filter(
                        provider ->
                                ProviderStatuses.ENABLED.equals(provider.getStatus())
                                        || ProviderStatuses.OBSOLETE.equals(provider.getStatus()))
                .peek(
                        provider -> {
                            provider.setMarket(config.getMarket());
                            provider.setCurrency(config.getCurrency());
                        });
    }

    private static List<Provider> getProviderConfigurationsForEnabledProviders(
            String folderForConfigurations) {

        return getProviderConfigurationFiles(folderForConfigurations).stream()
                .map(AgentInitialisationTest::readProviderConfiguration)
                .flatMap(AgentInitialisationTest::mapProviders)
                .collect(Collectors.toList());
    }

    private static Set<Module> getGuiceModulesToUse()
            throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        final Method buildForProductionMethod =
                AggregationModuleFactory.class.getDeclaredMethod(
                        "baseBuilder", AggregationServiceConfiguration.class, Environment.class);

        buildForProductionMethod.setAccessible(true);

        final ImmutableList.Builder<Module> modulesList =
                (ImmutableList.Builder<Module>)
                        buildForProductionMethod.invoke(null, configuration, ENVIRONMENT);

        Set<Module> modules = modulesList.build().stream().collect(Collectors.toSet());

        modules.add(
                new AbstractModule() {

                    @Provides
                    @Singleton
                    @Named("clusterConfigurations")
                    public Map<String, ClusterConfiguration> provideClusterConfigurations() {
                        return new HashMap<>();
                    }

                    @Provides
                    @Singleton
                    @Named("aggregatorConfiguration")
                    public Map<String, AggregatorConfiguration> providerAggregatorConfiguration() {
                        return new HashMap<>();
                    }

                    @Provides
                    @Singleton
                    @Named("clientConfigurationByClientKey")
                    public Map<String, ClientConfiguration> providerClientConfiguration() {
                        return new HashMap<>();
                    }

                    @Provides
                    @Singleton
                    @Named("clientConfigurationByName")
                    public Map<String, ClientConfiguration> providerClientConfigurationByName() {
                        return new HashMap<>();
                    }

                    @Override
                    protected void configure() {
                        bind(CryptoConfigurationsRepository.class)
                                .toInstance(
                                        new FakeCryptoConfigurationsRepository(
                                                new CryptoConfiguration()));
                    }
                });
        return modules;
    }

    private CredentialsRequest createCredentialsRequest(Provider provider) throws IOException {

        final Credentials credentials =
                MAPPER.readValue(
                        String.format(CREDENTIALS_OBJECT_TEMPLATE, provider.getName()),
                        Credentials.class);

        // This is to populate serializedFields field of Credentials object (which is normally done
        // by Main)
        credentials.setFields(credentials.getFields());

        return new ManualAuthenticateRequest(user, provider, credentials, true);
    }

    @BeforeClass
    public static void prepareForTest() {
        // given
        try {
            configuration = readConfiguration("etc/test.yml");
            providerConfigurationsForEnabledProviders =
                    getProviderConfigurationsForEnabledProviders(
                            "external/tink_backend/src/provider_configuration/data/seeding");

            agentCapabilities =
                    readCapabilities(
                            "external/tink_backend/src/provider_configuration/data/seeding/providers/capabilities/agent-capabilities.json");

            agentInitialisationTestConfig =
                    readTestConfiguration(
                            "src/integration/lib/src/test/java/se/tink/backend/aggregation/agents/agentfactory/initialisation/resources/test_config.yml");

            providerConfigurationsForEnabledProviders.sort(
                    (p1, p2) -> p1.getName().compareTo(p2.getName()));

            guiceModulesToUse = getGuiceModulesToUse();
            user = MAPPER.readValue(USER_OBJECT_TEMPLATE, User.class);

            hostConfiguration = mock(HostConfiguration.class);
            when(hostConfiguration.getClusterId()).thenReturn(TEST_CLUSTER_ID);

            injector = Guice.createInjector(guiceModulesToUse);
            agentFactory = injector.getInstance(AgentFactory.class);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public AgentContext createContext(CredentialsRequest credentialsRequest) {

        AgentContext context = mock(AgentContext.class);
        doReturn(TEST_CLUSTER_ID).when(context).getClusterId();
        doReturn("tink").when(context).getAppId();
        doReturn(mock(MetricRegistry.class)).when(context).getMetricRegistry();
        doReturn(false).when(context).isTestContext();
        doReturn(false).when(context).isWaitingOnConnectorTransactions();
        doReturn(mock(AggregatorInfo.class)).when(context).getAggregatorInfo();
        doReturn(new HashMap<>()).when(context).getTransactionCountByEnabledAccount();
        doReturn(new ByteArrayOutputStream()).when(context).getLogOutputStream();

        // Not easily mockable since we need the implementation of getAgentConfiguration ->
        // getAgentConfigurationDev (and the parameter for these methods comes from Agent)
        AgentConfigurationControllerable agentConfigurationController =
                new AgentConfigurationController(
                        mock(TppSecretsServiceClient.class),
                        configuration.getAgentsServiceConfiguration().getIntegrations(),
                        credentialsRequest.getProvider(),
                        context.getAppId(),
                        "clusterIdForSecretsService",
                        credentialsRequest.getCallbackUri());

        doReturn(agentConfigurationController).when(context).getAgentConfigurationController();
        doReturn(new FakeLogMasker()).when(context).getLogMasker();

        return context;
    }

    private void handleException(Exception e, Provider provider) {
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

    private Agent initialiseAgent(Provider provider) {
        try {
            // given
            CredentialsRequest credentialsRequest = createCredentialsRequest(provider);
            AgentContext context = createContext(credentialsRequest);

            // when
            return agentFactory.create(credentialsRequest, context);
        } catch (Exception e) {
            handleException(e, provider);
            return null;
        }
    }

    private void compareExpectedAndGivenAgentCapabilities(Provider provider) {
        Agent agent = initialiseAgent(provider);

        // Skip capability checks because we cannot do that for these agents
        if (agent instanceof DeprecatedRefreshExecutor) {
            return;
        }

        // given
        // Find given and expected agent capabilitie
        Set<String> givenAgentCapabilities = new HashSet<>();
        List<String> expectedAgentCapabilities = agentCapabilities.get(provider.getClassName());

        if (agent instanceof RefreshCreditCardAccountsExecutor) {
            givenAgentCapabilities.add("CREDIT_CARDS");
        }
        if (agent instanceof RefreshIdentityDataExecutor) {
            givenAgentCapabilities.add("IDENTITY_DATA");
        }
        if (agent instanceof RefreshCheckingAccountsExecutor) {
            givenAgentCapabilities.add("CHECKING_ACCOUNTS");
        }
        if (agent instanceof RefreshSavingsAccountsExecutor) {
            givenAgentCapabilities.add("SAVINGS_ACCOUNTS");
        }
        if (agent instanceof RefreshInvestmentAccountsExecutor) {
            givenAgentCapabilities.add("INVESTMENTS");
        }
        if (agent instanceof RefreshLoanAccountsExecutor) {
            boolean relatedGivenCapability = false;
            if (expectedAgentCapabilities.contains("LOANS")) {
                givenAgentCapabilities.add("LOANS");
                relatedGivenCapability = true;
            }
            if (expectedAgentCapabilities.contains("MORTGAGE_AGGREGATION")) {
                givenAgentCapabilities.add("MORTGAGE_AGGREGATION");
                relatedGivenCapability = true;
            }
            if (!relatedGivenCapability) {
                // Not MORTGAGE_AGGREGATION because LOANS is the new capability that covers
                // all, MORTGAGE_AGGREGATION is just there for backward compatibility
                givenAgentCapabilities.add("LOANS");
            }
        }
        if (agent instanceof TransferExecutor) {
            boolean relatedGivenCapability = false;
            if (expectedAgentCapabilities.contains("TRANSFERS")) {
                givenAgentCapabilities.add("TRANSFERS");
                relatedGivenCapability = true;
            }
            if (expectedAgentCapabilities.contains("PAYMENTS")) {
                givenAgentCapabilities.add("PAYMENTS");
                relatedGivenCapability = true;
            }
            if (!relatedGivenCapability) {
                // Not TRANSFERS because PAYMENTS and TRANSFERS are the same and PAYMENTS is
                // newer
                // TRANSFER is there just for backward compatibility
                givenAgentCapabilities.add("PAYMENTS");
            }
        }
        /*
        If agent is TransferExecutorNxgen, there is no way for us to determine if this agent
        has transfer/payments capability or not so we will not make any assertions on that

        Turned out that an agent implementing TransferExecutorNxgen interface does not prove
        that it should have the TRANSFER capability (see AxaAgent)
         */
        if (agent instanceof TransferExecutorNxgen) {
            if (expectedAgentCapabilities.contains("TRANSFERS")) {
                expectedAgentCapabilities.remove("TRANSFERS");
            }
            if (expectedAgentCapabilities.contains("PAYMENTS")) {
                expectedAgentCapabilities.remove("PAYMENTS");
            }
        }

        // then
        SetView<String> expectedButNotGiven =
                Sets.difference(new HashSet<>(expectedAgentCapabilities), givenAgentCapabilities);

        SetView<String> givenButNotExpected =
                Sets.difference(givenAgentCapabilities, new HashSet<>(expectedAgentCapabilities));

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
    private List<Provider> getProviders() {
        return providerConfigurationsForEnabledProviders.stream()
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
                getProviders().stream()
                        .filter(
                                provider ->
                                        !agentInitialisationTestConfig
                                                .getIgnoredAgentsForInitialisationTest()
                                                .contains(provider.getClassName()))
                        .collect(Collectors.toList());

        // given / when
        providers.parallelStream().forEach(this::initialiseAgent);

        /*
           What we want to test is to check whether we can initialise all agents without having
           an exception. For this reason, we don't have an explicit "then" block for this test
        */
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
                getProviders().stream()
                        .filter(
                                provider ->
                                        !agentInitialisationTestConfig
                                                .getIgnoredAgentsForInitialisationTest()
                                                .contains(provider.getClassName()))
                        .filter(
                                provider ->
                                        !agentInitialisationTestConfig
                                                .getIgnoredAgentsForCapabilityTest()
                                                .contains(provider.getClassName()))
                        .collect(Collectors.toList());

        // when / then
        providers.parallelStream().forEach(this::compareExpectedAndGivenAgentCapabilities);
    }
}
