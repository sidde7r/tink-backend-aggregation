package se.tink.backend.aggregation.agents.agentfactory.initialisation;

import static java.util.stream.Collectors.groupingBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
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
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.validation.Validation;
import javax.validation.Validator;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.agents.rpc.ProviderStatuses;
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

    private static final Logger logger = LoggerFactory.getLogger(AgentInitialisationTest.class);

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
            "{\"debugUntil\":695260800,\"providerLatency\":0,\"id\":\"refresh-test\",\"nextUpdate\":null,\"fields\":{},\"payload\":\"\",\"providerName\":\"%s\",\"sessionExpiryDate\":null,\"status\":\"CREATED\",\"statusPayload\":\"CREATED\",\"statusPrompt\":\"\",\"statusUpdated\":\"695260800\",\"supplementalInformation\":\"\",\"type\":\"PASSWORD\",\"updated\":\"695260800\",\"userId\":\"userId\",\"dataVersion\":1}";

    private static final String USER_OBJECT_TEMPLATE =
            "{\"flags\": [], \"flagsSerialized\": \"[]\", \"id\": \"userId\", \"profile\": {\"locale\": \"en_US\"}, \"username\": \"username\", \"debugUntil\": 695260800 }";

    private static User user;
    private static AggregationServiceConfiguration configuration;
    private static List<Provider> providerConfigurationsForEnabledProviders;
    private static Set<Module> guiceModulesToUse;
    private static HostConfiguration hostConfiguration;
    private static Injector injector;
    private static AgentFactory agentFactory;

    // These agents are reading secrets from k8s and this is not supported by this test
    private static ImmutableSet<String> ignoredK8SAgents =
            ImmutableSet.of(
                    "banks.se.collector.CollectorAgent",
                    "nxgen.dk.banks.jyske.JyskeNemidAgent",
                    "nxgen.dk.banks.jyske.JyskeKeyCardAgent",
                    "nxgen.se.openbanking.nordnet.NordnetAgent",
                    "nxgen.serviceproviders.banks.revolut.RevolutAgent",
                    "nxgen.es.openbanking.bbva.BbvaAgent");

    // These agents are temporarily ignored because these agents fail in the test
    // these agents will be investigated further
    private static ImmutableSet<String> temporarilyIgnoredAgents =
            ImmutableSet.of(
                    "nxgen.it.openbanking.bancasella.BancaSellaAgent",
                    "nxgen.nl.openbanking.knab.KnabAgent",
                    "nxgen.de.openbanking.fiducia.FiduciaAgent",
                    "nxgen.serviceproviders.openbanking.redsys.RedsysAgent",
                    "nxgen.de.banks.fints.FinTsAgent",
                    "nxgen.de.openbanking.fidor.FidorAgent",
                    "nxgen.nl.banks.openbanking.rabobank.RabobankAgent",
                    "banks.danskebank.v2.DanskeBankV2Agent",
                    "abnamro.AbnAmroAgent",
                    "nxgen.se.openbanking.alandsbanken.AlandsbankenAgent",
                    "nxgen.serviceproviders.banks.n26.N26Agent",
                    "nxgen.uk.openbanking.bankofireland.BankOfIrelandAgent",
                    "nxgen.dk.openbanking.sebkort.eurocard.EurocardDKAgent",
                    "nxgen.se.banks.nordea.partner.NordeaPartnerSeAgent",
                    "nxgen.dk.banks.nordeapartner.NordeaPartnerDkAgent",
                    "nxgen.no.banks.nordeapartner.NordeaPartnerNoAgent",
                    "nxgen.fi.banks.nordea.partner.NordeaPartnerFiAgent",
                    "nxgen.de.openbanking.postbank.PostbankAgent");

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
                .filter(provider -> ProviderStatuses.ENABLED.equals(provider.getStatus()))
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

    private void initialiseAgent(Provider provider) {
        try {
            // given
            CredentialsRequest credentialsRequest = createCredentialsRequest(provider);
            AgentContext context = createContext(credentialsRequest);

            // when
            Agent agent = agentFactory.create(credentialsRequest, context);
        } catch (Exception e) {
            logger.error(
                    "Agent "
                            + provider.getClassName()
                            + " could not be instantiated for provider "
                            + provider.getName());
            throw new RuntimeException(e);
        }
    }

    @Test
    public void whenEnabledProvidersAreGivenAgentFactoryShouldInstantiateAllEnabledAgents() {
        // given
        List<Provider> providers =
                providerConfigurationsForEnabledProviders.stream()
                        .filter(provider -> !provider.getName().toLowerCase().contains("test"))
                        .collect(groupingBy(Provider::getClassName))
                        .entrySet()
                        .stream()
                        .map(entry -> entry.getValue().get(0))
                        .filter(provider -> !ignoredK8SAgents.contains(provider.getClassName()))
                        .filter(
                                provider ->
                                        !temporarilyIgnoredAgents.contains(provider.getClassName()))
                        .collect(Collectors.toList());

        // given / when
        providers.parallelStream().forEach(this::initialiseAgent);

        /*
           What we want to test is to check whether we can initialise all agents without having
           an exception. For this reason, we don't have an explicit "then" block for this test
        */
    }
}
