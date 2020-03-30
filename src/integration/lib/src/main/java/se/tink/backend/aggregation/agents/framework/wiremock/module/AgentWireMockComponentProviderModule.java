package se.tink.backend.aggregation.agents.framework.wiremock.module;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.framework.wiremock.configuration.WireMockConfiguration;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.agentcontext.AgentContextProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.agentcontext.AgentContextProviderImpl;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.GeneratedValueProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.GeneratedValueProviderImpl;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.ConstantLocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.MockRandomValueGenerator;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.supplementalinformation.MockSupplementalInformationProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.supplementalinformation.SupplementalInformationProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.tinkhttpclient.TinkHttpClientProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.tinkhttpclient.WireMockTinkHttpClientProvider;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class AgentWireMockComponentProviderModule extends AbstractModule {

    private final TinkHttpClientProvider wireMockTinkHttpClientProvider;
    private final SupplementalInformationProvider mockSupplementalInformationProvider;
    private final GeneratedValueProvider generatedValueProvider;

    public AgentWireMockComponentProviderModule(
            CredentialsRequest request,
            AgentContext agentContext,
            AgentsServiceConfiguration configuration,
            WireMockConfiguration wireMockConfiguration) {

        this.wireMockTinkHttpClientProvider =
                new WireMockTinkHttpClientProvider(
                        request,
                        agentContext,
                        configuration.getSignatureKeyPair(),
                        wireMockConfiguration.getServerUrl());
        this.mockSupplementalInformationProvider =
                new MockSupplementalInformationProvider(wireMockConfiguration.getCallbackData());
        this.generatedValueProvider =
                new GeneratedValueProviderImpl(
                        new ConstantLocalDateTimeSource(), new MockRandomValueGenerator());
    }

    @Override
    protected void configure() {

        bind(TinkHttpClientProvider.class).toInstance(wireMockTinkHttpClientProvider);
        bind(SupplementalInformationProvider.class).toInstance(mockSupplementalInformationProvider);
        bind(AgentContextProvider.class).to(AgentContextProviderImpl.class);
        bind(GeneratedValueProvider.class).toInstance(generatedValueProvider);

        bind(AgentComponentProvider.class).in(Scopes.SINGLETON);
    }
}
