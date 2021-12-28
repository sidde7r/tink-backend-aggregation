package se.tink.backend.aggregation.nxgen.agents.componentproviders;

import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Optional;
import se.tink.agent.runtime.operation.ProviderImpl;
import se.tink.agent.runtime.operation.StaticBankCredentialsImpl;
import se.tink.agent.runtime.operation.UserImpl;
import se.tink.agent.sdk.operation.Provider;
import se.tink.agent.sdk.operation.StaticBankCredentials;
import se.tink.agent.sdk.operation.User;
import se.tink.agent.sdk.operation.http.ProxyProfiles;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.contexts.AgentAggregatorIdentifier;
import se.tink.backend.aggregation.agents.contexts.CompositeAgentContext;
import se.tink.backend.aggregation.agents.contexts.MetricContext;
import se.tink.backend.aggregation.agents.contexts.ProviderSessionCacheContext;
import se.tink.backend.aggregation.agents.contexts.SystemUpdater;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.agentcontext.AgentContextProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.GeneratedValueProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.http_proxy.ProxyProfilesProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.mockserverurl.MockServerUrlProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.storage.AgentTemporaryStorageProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.supplementalinformation.SupplementalInformationProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.tinkhttpclient.TinkHttpClientProvider;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.AgentTemporaryStorage;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.UserAvailability;
import se.tink.libraries.unleash.UnleashClient;
import se.tink.libraries.unleash.provider.UnleashClientProvider;

public class AgentComponentProvider
        implements TinkHttpClientProvider,
                SupplementalInformationProvider,
                AgentContextProvider,
                GeneratedValueProvider,
                UnleashClientProvider,
                AgentTemporaryStorageProvider,
                MockServerUrlProvider,
                ProxyProfilesProvider {

    private final TinkHttpClientProvider tinkHttpClientProvider;
    private final SupplementalInformationProvider supplementalInformationProvider;
    private final AgentContextProvider agentContextProvider;
    private final GeneratedValueProvider generatedValueProvider;
    private final UnleashClientProvider unleashClientProvider;
    private final AgentTemporaryStorageProvider agentTemporaryStorageProvider;
    private final MockServerUrlProvider mockServerUrlProvider;
    private final User user;
    private final StaticBankCredentials staticBankCredentials;
    private final Provider provider;
    private final ProxyProfilesProvider proxyProfilesProvider;

    @Inject
    public AgentComponentProvider(
            TinkHttpClientProvider tinkHttpClientProvider,
            SupplementalInformationProvider supplementalInformationProvider,
            AgentContextProvider agentContextProvider,
            GeneratedValueProvider generatedValueProvider,
            UnleashClientProvider unleashClientProvider,
            AgentTemporaryStorageProvider agentTemporaryStorageProvider,
            MockServerUrlProvider mockServerUrlProvider,
            ProxyProfilesProvider proxyProfilesProvider) {
        this.tinkHttpClientProvider = tinkHttpClientProvider;
        this.supplementalInformationProvider = supplementalInformationProvider;
        this.agentContextProvider = agentContextProvider;
        this.generatedValueProvider = generatedValueProvider;
        this.unleashClientProvider = unleashClientProvider;
        this.agentTemporaryStorageProvider = agentTemporaryStorageProvider;
        this.mockServerUrlProvider = mockServerUrlProvider;
        this.proxyProfilesProvider = proxyProfilesProvider;

        CredentialsRequest credentialsRequest = agentContextProvider.getCredentialsRequest();
        this.user = mapToSdkUser(credentialsRequest);
        this.staticBankCredentials = mapToSdkStaticBankCredentials(credentialsRequest);
        this.provider = mapToSdkProvider(credentialsRequest);
    }

    private User mapToSdkUser(CredentialsRequest credentialsRequest) {
        if (credentialsRequest == null) {
            return null;
        }
        String locale =
                Optional.ofNullable(credentialsRequest.getUser())
                        .map(se.tink.libraries.user.rpc.User::getLocale)
                        .orElse(null);
        UserAvailability userAvailability = credentialsRequest.getUserAvailability();

        if (userAvailability == null) {
            return new UserImpl(false, false, null, locale);
        }

        return new UserImpl(
                userAvailability.isUserPresent(),
                userAvailability.isUserAvailableForInteraction(),
                userAvailability.getOriginatingUserIp(),
                locale);
    }

    private StaticBankCredentials mapToSdkStaticBankCredentials(
            CredentialsRequest credentialsRequest) {
        if (credentialsRequest == null) {
            return null;
        }
        Credentials credentials = credentialsRequest.getCredentials();
        if (credentials == null) {
            return new StaticBankCredentialsImpl(new HashMap<>());
        }

        return new StaticBankCredentialsImpl(credentials.getFields());
    }

    private Provider mapToSdkProvider(CredentialsRequest credentialsRequest) {
        if (credentialsRequest == null) {
            return null;
        }
        se.tink.backend.agents.rpc.Provider rpcProvider = credentialsRequest.getProvider();

        if (rpcProvider == null) {
            return new ProviderImpl(null, null, null, null);
        }

        return new ProviderImpl(
                rpcProvider.getMarket(),
                rpcProvider.getName(),
                rpcProvider.getCurrency(),
                rpcProvider.getPayload());
    }

    @Override
    public TinkHttpClient getTinkHttpClient() {
        return tinkHttpClientProvider.getTinkHttpClient();
    }

    @Override
    public SupplementalInformationHelper getSupplementalInformationHelper() {
        return supplementalInformationProvider.getSupplementalInformationHelper();
    }

    @Override
    public SupplementalInformationController getSupplementalInformationController() {
        return supplementalInformationProvider.getSupplementalInformationController();
    }

    @Override
    public CredentialsRequest getCredentialsRequest() {
        return agentContextProvider.getCredentialsRequest();
    }

    @Override
    public CompositeAgentContext getContext() {
        return agentContextProvider.getContext();
    }

    @Override
    public MetricContext getMetricContext() {
        return agentContextProvider.getMetricContext();
    }

    @Override
    public SystemUpdater getSystemUpdater() {
        return agentContextProvider.getSystemUpdater();
    }

    @Override
    public AgentAggregatorIdentifier getAgentAggregatorIdentifier() {
        return agentContextProvider.getAgentAggregatorIdentifier();
    }

    @Override
    public ProviderSessionCacheContext getProviderSessionCacheContext() {
        return agentContextProvider.getProviderSessionCacheContext();
    }

    @Override
    public LocalDateTimeSource getLocalDateTimeSource() {
        return generatedValueProvider.getLocalDateTimeSource();
    }

    @Override
    public RandomValueGenerator getRandomValueGenerator() {
        return generatedValueProvider.getRandomValueGenerator();
    }

    @Override
    public UnleashClient getUnleashClient() {
        return unleashClientProvider.getUnleashClient();
    }

    @Override
    public AgentTemporaryStorage getAgentTemporaryStorage() {
        return agentTemporaryStorageProvider.getAgentTemporaryStorage();
    }

    @Override
    public Optional<String> getMockServerUrl() {
        return mockServerUrlProvider.getMockServerUrl();
    }

    public User getUser() {
        return user;
    }

    public StaticBankCredentials getStaticBankCredentials() {
        return staticBankCredentials;
    }

    public Provider getProvider() {
        return provider;
    }

    @Override
    public ProxyProfiles getProxyProfiles() {
        return proxyProfilesProvider.getProxyProfiles();
    }
}
