package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.integration;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexTestFixtures.APP_ID;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexTestFixtures.CERT_ID;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexTestFixtures.CLIENT_ID;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexTestFixtures.CLUSTER_ID;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexTestFixtures.ORIGINATING_USER_IP;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexTestFixtures.REDIRECT_URL;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import java.util.Collections;
import java.util.Optional;
import net.minidev.json.JSONObject;
import org.junit.Ignore;
import org.junit.Rule;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.framework.provider.ProviderConfigurationUtil;
import se.tink.backend.aggregation.agents.framework.wiremock.configuration.provider.socket.FakeBankSocket;
import se.tink.backend.aggregation.agents.framework.wiremock.configuration.provider.socket.MutableFakeBankSocket;
import se.tink.backend.aggregation.configuration.IntegrationsConfiguration;
import se.tink.backend.aggregation.configuration.ProviderConfig;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.fakelogmasker.FakeLogMasker;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.agentcontext.factory.AgentContextProviderFactory;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.agentcontext.factory.AgentContextProviderFactoryImpl;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.GeneratedValueProviderImpl;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.ActualLocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGeneratorImpl;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.mockserverurl.WireMockServerUrlProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.storage.AgentTemporaryStorageProviderImpl;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.supplementalinformation.factory.SupplementalInformationProviderFactory;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.supplementalinformation.factory.SupplementalInformationProviderFactoryImpl;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.tinkhttpclient.WireMockTinkHttpClientProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.unleashclient.UnleashClientProviderImpl;
import se.tink.backend.aggregation.nxgen.controllers.configuration.AgentConfigurationController;
import se.tink.backend.aggregation.nxgen.http.event.decision_strategy.AllowAlwaysRawBankDataEventCreationTriggerStrategy;
import se.tink.backend.aggregation.nxgen.storage.AgentTemporaryStorage;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsStatus;
import se.tink.backend.integration.tpp_secrets_service.client.iface.TppSecretsServiceClient;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.RefreshInformationRequest;
import se.tink.libraries.credentials.service.UserAvailability;
import se.tink.libraries.metrics.core.MetricBuckets;
import se.tink.libraries.metrics.registry.MetricRegistry;
import se.tink.libraries.metrics.types.counters.Counter;
import se.tink.libraries.metrics.types.histograms.Histogram;
import se.tink.libraries.metrics.types.timers.Timer;
import se.tink.libraries.unleash.UnleashClient;
import se.tink.libraries.user.rpc.User;
import se.tink.libraries.user.rpc.UserProfile;

@Ignore
public abstract class IntegrationTestBase {

    @Rule
    public WireMockRule wireMockRule =
            new WireMockRule(wireMockConfig().dynamicPort().dynamicHttpsPort());

    AgentComponentProvider createAgentComponentProvider(CredentialsRequest credentialsRequest) {
        final SupplementalInformationProviderFactory supplementalInformationProviderFactory =
                new SupplementalInformationProviderFactoryImpl();
        final AgentContextProviderFactory agentContextProviderFactory =
                new AgentContextProviderFactoryImpl();
        final SignatureKeyPair signatureKeyPair = new SignatureKeyPair();
        final AgentContext agentContext = createAgentContext(credentialsRequest);
        final FakeBankSocket fakeBankSocket =
                MutableFakeBankSocket.of(
                        "localhost:" + wireMockRule.port(),
                        "localhost:" + wireMockRule.httpsPort());

        return new AgentComponentProvider(
                new WireMockTinkHttpClientProvider(
                        credentialsRequest,
                        agentContext,
                        signatureKeyPair,
                        fakeBankSocket,
                        new AllowAlwaysRawBankDataEventCreationTriggerStrategy()),
                supplementalInformationProviderFactory.createSupplementalInformationProvider(
                        agentContext, credentialsRequest),
                agentContextProviderFactory.createAgentContextProvider(
                        credentialsRequest, agentContext),
                new GeneratedValueProviderImpl(
                        new ActualLocalDateTimeSource(), new RandomValueGeneratorImpl()),
                new UnleashClientProviderImpl(agentContext),
                new AgentTemporaryStorageProviderImpl(agentContext.getAgentTemporaryStorage()),
                new WireMockServerUrlProvider(fakeBankSocket));
    }

    CredentialsRequest createCredentialsRequest() {
        final User user = createUser();
        final Provider provider = getProvider();
        final Credentials credentials = createCredentials(user, provider);
        final UserAvailability userAvailability = createUserAvailability();

        return RefreshInformationRequest.builder()
                .user(user)
                .provider(provider)
                .credentials(credentials)
                .originatingUserIp(ORIGINATING_USER_IP)
                .userAvailability(userAvailability)
                .manual(false)
                .forceAuthenticate(false)
                .build();
    }

    private AgentContext createAgentContext(CredentialsRequest credentialsRequest) {
        final AgentContext agentContext = mock(AgentContext.class);

        final AgentConfigurationController agentConfigurationController =
                createAgentConfigurationController(credentialsRequest.getProvider());
        when(agentContext.getAgentConfigurationController())
                .thenReturn(agentConfigurationController);
        when(agentContext.getLogMasker()).thenReturn(new FakeLogMasker());

        final MetricRegistry metricRegistry = createMetricRegistry();
        when(agentContext.getMetricRegistry()).thenReturn(metricRegistry);

        UnleashClient unleashClient = mock(UnleashClient.class);
        when(unleashClient.isToggleEnable(any())).thenReturn(true);
        when(agentContext.getUnleashClient()).thenReturn(unleashClient);

        AgentTemporaryStorage agentTemporaryStorage = mock(AgentTemporaryStorage.class);
        when(agentContext.getAgentTemporaryStorage()).thenReturn(agentTemporaryStorage);

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
                CERT_ID,
                REDIRECT_URL);
    }

    private IntegrationsConfiguration createIntegrationsConfiguration(Provider provider) {
        final IntegrationsConfiguration integrationsConfigurationMock =
                mock(IntegrationsConfiguration.class);
        final JSONObject agentConfiguration = createConfiguration();

        when(integrationsConfigurationMock.getClientConfigurationAsObject(
                        provider.getFinancialInstitutionId(), APP_ID))
                .thenReturn(Optional.of(agentConfiguration));

        return integrationsConfigurationMock;
    }

    private JSONObject createConfiguration() {
        final JSONObject agentConfiguration = new JSONObject();
        agentConfiguration.put("clientId", CLIENT_ID);
        agentConfiguration.put("clientSecret", "secret");
        agentConfiguration.put("redirectUrl", REDIRECT_URL);

        return agentConfiguration;
    }

    private static User createUser() {
        User user = new User();
        user.setId("123");
        user.setProfile(new UserProfile());
        user.setFlags(Collections.emptyList());

        return user;
    }

    private static Provider getProvider() {
        final ProviderConfig marketProviders =
                ProviderConfigurationUtil.readProvidersConfiguration("uk");
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

    private static UserAvailability createUserAvailability() {
        UserAvailability userAvailability = new UserAvailability();
        userAvailability.setUserAvailableForInteraction(false);
        userAvailability.setUserPresent(false);
        userAvailability.setOriginatingUserIp(ORIGINATING_USER_IP);

        return userAvailability;
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
}
