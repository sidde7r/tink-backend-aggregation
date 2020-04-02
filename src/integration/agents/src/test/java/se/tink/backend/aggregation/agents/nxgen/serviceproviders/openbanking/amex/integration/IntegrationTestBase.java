package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.integration;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexTestFixtures.APP_ID;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexTestFixtures.AUTHORIZE_URL;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexTestFixtures.CLIENT_ID;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexTestFixtures.CLUSTER_ID;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexTestFixtures.REDIRECT_URL;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import org.junit.Rule;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsStatus;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.configuration.AmexConfiguration;
import se.tink.backend.aggregation.configuration.IntegrationsConfiguration;
import se.tink.backend.aggregation.configuration.ProviderConfig;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.logmasker.LogMaskerImpl;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.agentcontext.factory.AgentContextProviderFactory;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.agentcontext.factory.AgentContextProviderFactoryImpl;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.GeneratedValueProviderImpl;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.ActualLocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGeneratorImpl;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.supplementalinformation.factory.SupplementalInformationProviderFactory;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.supplementalinformation.factory.SupplementalInformationProviderFactoryImpl;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.tinkhttpclient.factory.NextGenTinkHttpClientProviderFactory;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.tinkhttpclient.factory.TinkHttpClientProviderFactory;
import se.tink.backend.aggregation.nxgen.controllers.configuration.AgentConfigurationController;
import se.tink.backend.integration.tpp_secrets_service.client.iface.TppSecretsServiceClient;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.RefreshInformationRequest;
import se.tink.libraries.metrics.core.MetricBuckets;
import se.tink.libraries.metrics.registry.MetricRegistry;
import se.tink.libraries.metrics.types.counters.Counter;
import se.tink.libraries.metrics.types.histograms.Histogram;
import se.tink.libraries.metrics.types.timers.Timer;
import se.tink.libraries.user.rpc.User;
import se.tink.libraries.user.rpc.UserProfile;

public abstract class IntegrationTestBase {

    private static final String LOCAL_URL = "http://localhost:%s";

    @Rule
    public WireMockRule wireMockRule =
            new WireMockRule(wireMockConfig().dynamicPort().dynamicHttpsPort());

    AgentComponentProvider createAgentComponentProvider(CredentialsRequest credentialsRequest) {
        final TinkHttpClientProviderFactory tinkHttpClientProviderFactory =
                new NextGenTinkHttpClientProviderFactory();
        final SupplementalInformationProviderFactory supplementalInformationProviderFactory =
                new SupplementalInformationProviderFactoryImpl();
        final AgentContextProviderFactory agentContextProviderFactory =
                new AgentContextProviderFactoryImpl();
        final SignatureKeyPair signatureKeyPair = new SignatureKeyPair();
        final AgentContext agentContext = createAgentContext(credentialsRequest);

        return new AgentComponentProvider(
                tinkHttpClientProviderFactory.createTinkHttpClientProvider(
                        credentialsRequest, agentContext, signatureKeyPair),
                supplementalInformationProviderFactory.createSupplementalInformationProvider(
                        agentContext, credentialsRequest),
                agentContextProviderFactory.createAgentContextProvider(
                        credentialsRequest, agentContext),
                new GeneratedValueProviderImpl(
                        new ActualLocalDateTimeSource(), new RandomValueGeneratorImpl()));
    }

    CredentialsRequest createCredentialsRequest() {
        final User user = createUser();
        final Provider provider = getProvider();
        final Credentials credentials = createCredentials(user, provider);

        return new RefreshInformationRequest(user, provider, credentials, false);
    }

    private AgentContext createAgentContext(CredentialsRequest credentialsRequest) {
        final AgentContext agentContext = mock(AgentContext.class);

        final AgentConfigurationController agentConfigurationController =
                createAgentConfigurationController(credentialsRequest.getProvider());
        when(agentContext.getAgentConfigurationController())
                .thenReturn(agentConfigurationController);
        when(agentContext.getLogMasker()).thenReturn(LogMaskerImpl.builder().build());

        final MetricRegistry metricRegistry = createMetricRegistry();
        when(agentContext.getMetricRegistry()).thenReturn(metricRegistry);

        return agentContext;
    }

    private AgentConfigurationController createAgentConfigurationController(Provider provider) {
        final IntegrationsConfiguration integrationsConfiguration =
                createIntegrationsConfiguration(provider);

        return new AgentConfigurationController(
                mock(TppSecretsServiceClient.class),
                integrationsConfiguration,
                provider,
                APP_ID,
                CLUSTER_ID,
                REDIRECT_URL);
    }

    private IntegrationsConfiguration createIntegrationsConfiguration(Provider provider) {
        final IntegrationsConfiguration integrationsConfigurationMock =
                mock(IntegrationsConfiguration.class);
        final AmexConfiguration amexConfiguration = createConfiguration();

        when(integrationsConfigurationMock.getClientConfigurationAsObject(
                        provider.getFinancialInstitutionId(), APP_ID))
                .thenReturn(Optional.of(amexConfiguration));

        return integrationsConfigurationMock;
    }

    private AmexConfiguration createConfiguration() {
        final AmexConfiguration amexConfiguration = new AmexConfiguration();
        amexConfiguration.setClientId(CLIENT_ID);
        amexConfiguration.setClientSecret("secret");
        amexConfiguration.setGrantAccessJourneyUrl(AUTHORIZE_URL);
        amexConfiguration.setRedirectUrl(REDIRECT_URL);
        amexConfiguration.setServerUrl(String.format(LOCAL_URL, wireMockRule.port()));

        return amexConfiguration;
    }

    private static User createUser() {
        User user = new User();
        user.setId("123");
        user.setProfile(new UserProfile());
        user.setFlags(Collections.emptyList());

        return user;
    }

    private static Provider getProvider() {
        final ProviderConfig marketProviders = readProvidersConfiguration();
        final Provider provider = marketProviders.getProvider("uk-amex-ob");
        provider.setMarket(marketProviders.getMarket());
        provider.setCurrency(marketProviders.getCurrency());

        return provider;
    }

    private static Credentials createCredentials(User user, Provider provider) {
        final Credentials credentials = new Credentials();
        credentials.setFields(Collections.emptyMap());
        credentials.setId("987");
        credentials.setUserId(user.getId());
        credentials.setStatus(CredentialsStatus.CREATED);
        credentials.setType(provider.getCredentialsType());
        credentials.setProviderName(provider.getName());

        return credentials;
    }

    private static MetricRegistry createMetricRegistry() {
        final MetricBuckets metricBuckets = new MetricBuckets(Collections.emptyList());
        final Timer timer = new Timer(metricBuckets);
        final Counter counter = new Counter();
        final Histogram histogram = new Histogram(metricBuckets);

        final MetricRegistry metricRegistryMock = mock(MetricRegistry.class);
        when(metricRegistryMock.timer(any())).thenReturn(timer);
        when(metricRegistryMock.meter(any())).thenReturn(counter);
        when(metricRegistryMock.histogram(any(), anyList())).thenReturn(histogram);

        return metricRegistryMock;
    }

    private static ProviderConfig readProvidersConfiguration() {
        final File providersFile = new File("data/seeding/providers-uk.json");
        final ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(providersFile, ProviderConfig.class);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
