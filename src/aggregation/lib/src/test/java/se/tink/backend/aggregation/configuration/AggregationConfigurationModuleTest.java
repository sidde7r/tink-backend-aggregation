package se.tink.backend.aggregation.configuration;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.configuration.guice.modules.AggregationConfigurationModule;
import se.tink.backend.aggregation.configuration.models.AggregationServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.strategy.AgentStrategyFactory;

public class AggregationConfigurationModuleTest {

    private Injector injector;

    @Before
    public void setup() {

        injector =
                Guice.createInjector(
                        new AggregationConfigurationModule(new AggregationServiceConfiguration()));
    }

    @Test
    public void ensure_AgentStrategyFactory_IsBound() {
        injector.getInstance(AgentStrategyFactory.class);
    }

    @Test
    public void ensure_AgentsServiceConfiguration_IsBound() {
        injector.getProvider(AgentsServiceConfiguration.class);
    }
}
