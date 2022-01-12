package se.tink.backend.aggregation.agents.module;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.agentcontext.AgentContextProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.agentcontext.AgentContextProviderImpl;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.eidas.ProductionQSealcSignerProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.eidas.QSealcSignerProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.GeneratedValueProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.GeneratedValueProviderImpl;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.ActualLocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGeneratorImpl;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.http_proxy.ProductionProxyProfilesProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.http_proxy.ProxyProfilesProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.mockserverurl.EmptyMockServerUrlProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.mockserverurl.MockServerUrlProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.storage.AgentTemporaryStorageProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.storage.AgentTemporaryStorageProviderImpl;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.supplementalinformation.SupplementalInformationProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.supplementalinformation.SupplementalInformationProviderImpl;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.tinkhttpclient.NextGenTinkHttpClientProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.tinkhttpclient.TinkHttpClientProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.unleashclient.UnleashClientProviderImpl;
import se.tink.backend.aggregation.nxgen.storage.AgentTemporaryStorage;
import se.tink.libraries.unleash.provider.UnleashClientProvider;

/** Module containing basic dependencies for running agent in production environment. */
@RequiredArgsConstructor
public final class AgentComponentProviderModule extends AbstractModule {

    private final AgentTemporaryStorage agentTemporaryStorage;

    @Override
    protected void configure() {
        bind(TinkHttpClientProvider.class).to(NextGenTinkHttpClientProvider.class);
        bind(MockServerUrlProvider.class).to(EmptyMockServerUrlProvider.class);
        bind(SupplementalInformationProvider.class).to(SupplementalInformationProviderImpl.class);
        bind(AgentContextProvider.class).to(AgentContextProviderImpl.class);
        bind(RandomValueGenerator.class).to(RandomValueGeneratorImpl.class);
        bind(LocalDateTimeSource.class).to(ActualLocalDateTimeSource.class);
        bind(GeneratedValueProvider.class).to(GeneratedValueProviderImpl.class);
        bind(UnleashClientProvider.class).to(UnleashClientProviderImpl.class);
        bind(AgentTemporaryStorage.class).toInstance(agentTemporaryStorage);
        bind(AgentTemporaryStorageProvider.class).to(AgentTemporaryStorageProviderImpl.class);
        bind(AgentComponentProvider.class).in(Scopes.SINGLETON);
        bind(ProxyProfilesProvider.class).to(ProductionProxyProfilesProvider.class);
        bind(QSealcSignerProvider.class).to(ProductionQSealcSignerProvider.class);
    }
}
