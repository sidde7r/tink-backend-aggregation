package se.tink.backend.aggregation.nxgen.agents.componentproviders;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.contexts.AgentAggregatorIdentifier;
import se.tink.backend.aggregation.agents.contexts.CompositeAgentContext;
import se.tink.backend.aggregation.agents.contexts.MetricContext;
import se.tink.backend.aggregation.agents.contexts.ProviderSessionCacheContext;
import se.tink.backend.aggregation.agents.contexts.SupplementalRequester;
import se.tink.backend.aggregation.agents.contexts.SystemUpdater;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.EncapClient;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.EncapConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.agentcontext.AgentContextProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.encapclient.EncapClientProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.GeneratedValueProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.supplementalinformation.SupplementalInformationProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.tinkhttpclient.TinkHttpClientProvider;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.utils.deviceprofile.DeviceProfile;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class AgentComponentProvider
        implements TinkHttpClientProvider,
                SupplementalInformationProvider,
                AgentContextProvider,
                GeneratedValueProvider,
                EncapClientProvider {

    private final TinkHttpClientProvider tinkHttpClientProvider;
    private final SupplementalInformationProvider supplementalInformationProvider;
    private final AgentContextProvider agentContextProvider;
    private final GeneratedValueProvider generatedValueProvider;
    private final EncapClientProvider encapClientProvider;

    @Inject
    public AgentComponentProvider(
            TinkHttpClientProvider tinkHttpClientProvider,
            SupplementalInformationProvider supplementalInformationProvider,
            AgentContextProvider agentContextProvider,
            GeneratedValueProvider generatedValueProvider,
            EncapClientProvider encapClientProvider) {
        this.tinkHttpClientProvider = tinkHttpClientProvider;
        this.supplementalInformationProvider = supplementalInformationProvider;
        this.agentContextProvider = agentContextProvider;
        this.generatedValueProvider = generatedValueProvider;
        this.encapClientProvider = encapClientProvider;
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
    public SupplementalRequester getSupplementalRequester() {
        return agentContextProvider.getSupplementalRequester();
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
    public EncapClient getEncapClient(
            PersistentStorage persistentStorage,
            EncapConfiguration configuration,
            DeviceProfile deviceProfile,
            TinkHttpClient httpClient) {
        return encapClientProvider.getEncapClient(
                persistentStorage, configuration, deviceProfile, httpClient);
    }
}
