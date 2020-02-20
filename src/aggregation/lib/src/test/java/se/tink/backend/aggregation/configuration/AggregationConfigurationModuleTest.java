package se.tink.backend.aggregation.configuration;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.configuration.guice.modules.AggregationConfigurationModule;
import se.tink.backend.aggregation.configuration.models.AggregationServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.agentcontext.factory.AgentContextProviderFactory;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.supplementalinformation.factory.SupplementalInformationProviderFactory;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.tinkhttpclient.factory.TinkHttpClientProviderFactory;

public class AggregationConfigurationModuleTest {

    private Injector injector;

    @Before
    public void setup() {

        injector =
                Guice.createInjector(
                        new AggregationConfigurationModule(new AggregationServiceConfiguration()));
    }

    @Test
    public void ensure_TinkHttpClientProviderFactory_IsBound() {
        injector.getInstance(TinkHttpClientProviderFactory.class);
    }

    @Test
    public void ensure_SupplementalInformationProviderFactory_IsBound() {
        injector.getInstance(SupplementalInformationProviderFactory.class);
    }

    @Test
    public void ensure_AgentContextProviderFactory_IsBound() {
        injector.getInstance(AgentContextProviderFactory.class);
    }

    @Test
    public void ensure_AgentsServiceConfiguration_IsBound() {
        injector.getProvider(AgentsServiceConfiguration.class);
    }
}
