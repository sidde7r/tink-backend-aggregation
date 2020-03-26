package se.tink.backend.aggregation.agents.agentfactory.production;

import com.google.inject.AbstractModule;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.agentcontext.factory.AgentContextProviderFactory;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.agentcontext.factory.AgentContextProviderFactoryImpl;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.GeneratedValueProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.GeneratedValueProviderImpl;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.ActualLocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGeneratorImpl;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.supplementalinformation.factory.SupplementalInformationProviderFactory;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.supplementalinformation.factory.SupplementalInformationProviderFactoryImpl;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.tinkhttpclient.factory.NextGenTinkHttpClientProviderFactory;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.tinkhttpclient.factory.TinkHttpClientProviderFactory;

public class ProductionModule extends AbstractModule {

    private final AgentsServiceConfiguration configuration;

    public ProductionModule(AgentsServiceConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    protected void configure() {
        bind(TinkHttpClientProviderFactory.class).to(NextGenTinkHttpClientProviderFactory.class);
        bind(SupplementalInformationProviderFactory.class)
                .to(SupplementalInformationProviderFactoryImpl.class);
        bind(AgentContextProviderFactory.class).to(AgentContextProviderFactoryImpl.class);
        bind(GeneratedValueProvider.class).to(GeneratedValueProviderImpl.class);
        bind(LocalDateTimeSource.class).to(ActualLocalDateTimeSource.class);
        bind(RandomValueGenerator.class).to(RandomValueGeneratorImpl.class);
        bind(AgentsServiceConfiguration.class).toInstance(configuration);
    }
}
