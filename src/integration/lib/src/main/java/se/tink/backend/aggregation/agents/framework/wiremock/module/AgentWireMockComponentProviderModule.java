package se.tink.backend.aggregation.agents.framework.wiremock.module;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.framework.wiremock.configuration.WireMockConfiguration;
import se.tink.backend.aggregation.agents.framework.wiremock.configuration.provider.socket.FakeBankSocket;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.agentcontext.AgentContextProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.agentcontext.AgentContextProviderImpl;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.eidas.QSealcSignerProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.eidas.WiremockQSealcSignerProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.GeneratedValueProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.GeneratedValueProviderImpl;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.ConstantLocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.MockRandomValueGenerator;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.http_proxy.ProxyProfilesProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.http_proxy.WiremockProxyProfilesProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.mockserverurl.MockServerUrlProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.mockserverurl.WireMockServerUrlProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.supplementalinformation.MockSupplementalInformationProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.supplementalinformation.SupplementalInformationProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.tinkhttpclient.TinkHttpClientProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.tinkhttpclient.WireMockTinkHttpClientProvider;
import se.tink.backend.aggregation.nxgen.raw_data_events.decision_strategy.AllowAlwaysRawBankDataEventCreationTriggerStrategy;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.unleash.provider.FakeUnleashClientWithEnabledTogglesProvider;
import se.tink.libraries.unleash.provider.UnleashClientProvider;

public final class AgentWireMockComponentProviderModule extends AbstractModule {

    private final TinkHttpClientProvider wireMockTinkHttpClientProvider;
    private final MockServerUrlProvider wireMockMockServerUrlProvider;
    private final SupplementalInformationProvider mockSupplementalInformationProvider;
    private final GeneratedValueProvider generatedValueProvider;
    private final FakeBankSocket fakeBankSocket;

    public AgentWireMockComponentProviderModule(
            CredentialsRequest request,
            AgentContext agentContext,
            AgentsServiceConfiguration configuration,
            WireMockConfiguration wireMockConfiguration,
            FakeBankSocket fakeBankSocket) {

        this.wireMockTinkHttpClientProvider =
                new WireMockTinkHttpClientProvider(
                        request,
                        agentContext,
                        configuration.getSignatureKeyPair(),
                        fakeBankSocket,
                        new AllowAlwaysRawBankDataEventCreationTriggerStrategy());
        this.wireMockMockServerUrlProvider = new WireMockServerUrlProvider(fakeBankSocket);
        this.mockSupplementalInformationProvider =
                new MockSupplementalInformationProvider(wireMockConfiguration.getCallbackData());
        this.generatedValueProvider =
                new GeneratedValueProviderImpl(
                        new ConstantLocalDateTimeSource(), new MockRandomValueGenerator());
        this.fakeBankSocket = fakeBankSocket;
    }

    @Override
    protected void configure() {
        bind(TinkHttpClientProvider.class).toInstance(wireMockTinkHttpClientProvider);
        bind(MockServerUrlProvider.class).toInstance(wireMockMockServerUrlProvider);
        bind(SupplementalInformationProvider.class).toInstance(mockSupplementalInformationProvider);
        bind(AgentContextProvider.class).to(AgentContextProviderImpl.class);
        bind(GeneratedValueProvider.class).toInstance(generatedValueProvider);
        bind(FakeBankSocket.class).toInstance(fakeBankSocket);
        bind(AgentComponentProvider.class).in(Scopes.SINGLETON);
        bind(UnleashClientProvider.class).to(FakeUnleashClientWithEnabledTogglesProvider.class);
        bind(ProxyProfilesProvider.class).to(WiremockProxyProfilesProvider.class);
        bind(QSealcSignerProvider.class).to(WiremockQSealcSignerProvider.class);
    }
}
