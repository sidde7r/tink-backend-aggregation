package se.tink.backend.aggregation.agents.banks.seb.mortgage;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provider;
import org.junit.Test;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.CreateProductExecutor;
import se.tink.backend.common.config.SEBMortgageIntegrationConfiguration;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.libraries.metrics.MetricRegistry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SEBCreatableProductModuleTest {
    /**
     * This test ensures that we can instantiate all dependencies in the tree for a `CreateProductExecutor` in SEB.
     */
    @Test
    public void resolvesDependencies() throws Exception {
        AgentContext context = mock(AgentContext.class);
        when(context.getMetricRegistry()).thenReturn(mock(MetricRegistry.class));
        SEBCreatableProductModule sebCreatableProductModule = new SEBCreatableProductModule(context,
                mock(Credentials.class));

        // Api configuration instance is required in order for provider to be created
        sebCreatableProductModule.setMortgageConfiguration(mock(SEBMortgageIntegrationConfiguration.class));

        // Create injector and get executor
        Injector injector = Guice.createInjector(sebCreatableProductModule);
        Provider<CreateProductExecutor> provider = injector.getProvider(CreateProductExecutor.class);

        // Ensure provider can resolve instance
        assertThat(provider.get()).isNotNull();
    }
}
