package se.tink.backend.aggregation.agents.module;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.agentcontext.AgentContextProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.agentcontext.AgentContextProviderImpl;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.encapclient.Encap3ClientProviderImpl;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.encapclient.EncapClientProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.GeneratedValueProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.GeneratedValueProviderImpl;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.ActualLocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGeneratorImpl;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.supplementalinformation.SupplementalInformationProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.supplementalinformation.SupplementalInformationProviderImpl;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.tinkhttpclient.NextGenTinkHttpClientProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.tinkhttpclient.TinkHttpClientProvider;

/** Module containing basic dependencies for running agent in production environment. */
public final class AgentComponentProviderModule extends AbstractModule {

    @Override
    protected void configure() {

        bind(TinkHttpClientProvider.class).to(NextGenTinkHttpClientProvider.class);
        bind(SupplementalInformationProvider.class).to(SupplementalInformationProviderImpl.class);
        bind(AgentContextProvider.class).to(AgentContextProviderImpl.class);
        bind(RandomValueGenerator.class).to(RandomValueGeneratorImpl.class);
        bind(LocalDateTimeSource.class).to(ActualLocalDateTimeSource.class);
        bind(GeneratedValueProvider.class).to(GeneratedValueProviderImpl.class);
        bind(EncapClientProvider.class).to(Encap3ClientProviderImpl.class);

        bind(AgentComponentProvider.class).in(Scopes.SINGLETON);
    }
}
