package se.tink.backend.aggregation.agents.agentfactory.utils;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.Validation;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.agents.agentfactory.iface.AgentFactory;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.api.AggregatorInfo;
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
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.metrics.registry.MetricRegistry;
import se.tink.libraries.user.rpc.User;

public class AgentInitialisor {

    private static final Environment ENVIRONMENT =
            new Environment(
                    "test",
                    Jackson.newObjectMapper(),
                    Validation.buildDefaultValidatorFactory().getValidator(),
                    new com.codahale.metrics.MetricRegistry(),
                    ClassLoader.getSystemClassLoader());

    private final String testClusterId;
    private final AggregationServiceConfiguration aggregationServiceConfiguration;
    private final String credentialsTemplate;
    private final User user;
    private final AgentFactory agentFactory;

    public AgentInitialisor(
            String aggregationServiceConfigurationFilePath,
            String credentialsTemplateFilePath,
            String userTemplateFilePath) {
        this.testClusterId = "testCluster-DummyId";

        try {
            credentialsTemplate =
                    new String(
                            Files.readAllBytes(Paths.get(credentialsTemplateFilePath)),
                            StandardCharsets.UTF_8);

            user =
                    new ObjectMapper()
                            .readValue(
                                    new String(
                                            Files.readAllBytes(Paths.get(userTemplateFilePath)),
                                            StandardCharsets.UTF_8),
                                    User.class);

            aggregationServiceConfiguration =
                    readAggregationServiceConfigurationForTest(
                            aggregationServiceConfigurationFilePath);

            Injector injector = Guice.createInjector(getGuiceModulesToUse());
            agentFactory = injector.getInstance(AgentFactory.class);
        } catch (IOException | ConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public Agent initialiseAgent(Provider provider) {
        try {
            CredentialsRequest credentialsRequest = createCredentialsRequest(provider);
            AgentContext context = createContext(credentialsRequest);
            return agentFactory.create(credentialsRequest, context);
        } catch (ReflectiveOperationException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private AggregationServiceConfiguration readAggregationServiceConfigurationForTest(
            String filePath) throws IOException, ConfigurationException {

        ConfigurationFactory<AggregationServiceConfiguration> configurationFactory =
                new ConfigurationFactory<>(
                        AggregationServiceConfiguration.class,
                        Validation.buildDefaultValidatorFactory().getValidator(),
                        Jackson.newObjectMapper(),
                        "");

        return configurationFactory.build(new File(filePath));
    }

    private AgentContext createContext(CredentialsRequest credentialsRequest) {

        AgentContext context = mock(AgentContext.class);
        doReturn(testClusterId).when(context).getClusterId();
        doReturn("tink").when(context).getAppId();
        doReturn(mock(MetricRegistry.class)).when(context).getMetricRegistry();
        doReturn(false).when(context).isTestContext();
        doReturn(false).when(context).isWaitingOnConnectorTransactions();
        doReturn(AggregatorInfo.getAggregatorForTesting()).when(context).getAggregatorInfo();
        doReturn(new HashMap<>()).when(context).getTransactionCountByEnabledAccount();
        doReturn(new ByteArrayOutputStream()).when(context).getLogOutputStream();
        doReturn(mock(Catalog.class)).when(context).getCatalog();

        // Not easily mockable since we need the implementation of getAgentConfiguration ->
        // getAgentConfigurationDev (and the parameter for these methods comes from Agent)
        AgentConfigurationControllerable agentConfigurationController =
                new AgentConfigurationController(
                        mock(TppSecretsServiceClient.class),
                        aggregationServiceConfiguration
                                .getAgentsServiceConfiguration()
                                .getIntegrations(),
                        credentialsRequest.getProvider(),
                        context.getAppId(),
                        "clusterIdForSecretsService",
                        credentialsRequest.getCallbackUri());

        doReturn(agentConfigurationController).when(context).getAgentConfigurationController();
        doReturn(new FakeLogMasker()).when(context).getLogMasker();

        return context;
    }

    private CredentialsRequest createCredentialsRequest(Provider provider) throws IOException {

        final Credentials credentials =
                new ObjectMapper()
                        .readValue(
                                String.format(credentialsTemplate, provider.getName()),
                                Credentials.class);

        // This is to populate serializedFields field of Credentials object (which is normally done
        // by Main)
        credentials.setFields(credentials.getFields());

        return new ManualAuthenticateRequest(user, provider, credentials, true);
    }

    private Set<Module> getGuiceModulesToUse() {

        final ImmutableList.Builder<Module> modulesList =
                AggregationModuleFactory.productionBuilder(
                        aggregationServiceConfiguration, ENVIRONMENT);

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
}
