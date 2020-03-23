package se.tink.backend.aggregation.agents.agentfactory.wiremocked;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import java.util.Map;
import se.tink.backend.aggregation.agents.framework.wiremock.configuration.WireMockConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.agentcontext.factory.AgentContextProviderFactory;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.agentcontext.factory.AgentContextProviderFactoryImpl;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.GeneratedValueProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.GeneratedValueProviderImpl;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.ConstantLocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.MockRandomValueGenerator;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.supplementalinformation.factory.MockSupplementalInformationProviderFactory;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.supplementalinformation.factory.SupplementalInformationProviderFactory;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.tinkhttpclient.factory.TinkHttpClientProviderFactory;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.tinkhttpclient.factory.WireMockTinkHttpClientProviderFactory;

public class WiremockedModule extends AbstractModule {

    private final WireMockConfiguration wireMockConfiguration;

    public WiremockedModule(final WireMockConfiguration wireMockConfiguration) {
        this.wireMockConfiguration = wireMockConfiguration;
    }

    @Override
    protected void configure() {
        bind(TinkHttpClientProviderFactory.class).to(WireMockTinkHttpClientProviderFactory.class);
        bind(String.class)
                .annotatedWith(Names.named("wireMockServerHost"))
                .toInstance(wireMockConfiguration.getServerUrl());

        bind(SupplementalInformationProviderFactory.class)
                .to(MockSupplementalInformationProviderFactory.class);
        bind(new TypeLiteral<Map<String, String>>() {})
                .annotatedWith(Names.named("mockCallbackData"))
                .toInstance(wireMockConfiguration.getCallbackData());

        bind(AgentContextProviderFactory.class).to(AgentContextProviderFactoryImpl.class);
        bind(GeneratedValueProvider.class).to(GeneratedValueProviderImpl.class);
        bind(LocalDateTimeSource.class).to(ConstantLocalDateTimeSource.class);
        bind(RandomValueGenerator.class).to(MockRandomValueGenerator.class);
    }
}
