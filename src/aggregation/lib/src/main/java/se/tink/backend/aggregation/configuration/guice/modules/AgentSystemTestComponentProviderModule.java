package se.tink.backend.aggregation.configuration.guice.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.framework.wiremock.configuration.provider.socket.FakeBankSocket;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.agentcontext.AgentContextProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.agentcontext.AgentContextProviderImpl;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.GeneratedValueProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.GeneratedValueProviderImpl;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.ConstantLocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.MockRandomValueGenerator;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.tinkhttpclient.TinkHttpClientProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.tinkhttpclient.WireMockTinkHttpClientProvider;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class AgentSystemTestComponentProviderModule extends AbstractModule {

    private final TinkHttpClientProvider wireMockTinkHttpClientProvider;
    private final GeneratedValueProvider generatedValueProvider;
    private final FakeBankSocket fakeBankSocket;

    public AgentSystemTestComponentProviderModule(
            CredentialsRequest request,
            AgentContext agentContext,
            AgentsServiceConfiguration configuration,
            FakeBankSocket fakeBankSocket) {

        this.wireMockTinkHttpClientProvider =
                new WireMockTinkHttpClientProvider(
                        request, agentContext, configuration.getSignatureKeyPair(), fakeBankSocket);
        this.generatedValueProvider =
                new GeneratedValueProviderImpl(
                        new ConstantLocalDateTimeSource(), new MockRandomValueGenerator());
        this.fakeBankSocket = fakeBankSocket;
    }

    @Override
    protected void configure() {
        bind(TinkHttpClientProvider.class).toInstance(wireMockTinkHttpClientProvider);
        bind(AgentContextProvider.class).to(AgentContextProviderImpl.class);
        bind(GeneratedValueProvider.class).toInstance(generatedValueProvider);
        bind(FakeBankSocket.class).toInstance(fakeBankSocket);
        bind(AgentComponentProvider.class).in(Scopes.SINGLETON);
    }
}
