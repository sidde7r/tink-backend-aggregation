package se.tink.backend.aggregation.workers.agent_metrics;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.agents.rpc.CredentialsStatus;
import se.tink.backend.aggregation.configuration.models.ProviderTierConfiguration;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.libraries.metrics.collection.MetricCollector;
import se.tink.libraries.metrics.registry.MetricRegistry;

public class AgentWorkerMetricReporterTest {

    private MetricCollector collector;
    private MetricRegistry registry;
    private AgentWorkerMetricReporter reporter;
    private AgentWorkerCommandContext context;

    @Before
    public void setUp() {
        collector = new MetricCollector();
        registry = new MetricRegistry(collector);
        reporter = new AgentWorkerMetricReporter(registry, new ProviderTierConfiguration());

        context = mock(AgentWorkerCommandContext.class, Mockito.RETURNS_DEEP_STUBS);
    }

    @Test
    public void TestSuccessfulAgentFlow() {
        when(context.getRequest().getCredentials().getStatus())
                .thenReturn(CredentialsStatus.UPDATING);
        when(context.getRequest().getProvider().getName()).thenReturn("ob-seb-test");
        when(context.getRequest().getProvider().getMarket()).thenReturn("SE");

        reporter.observe(context, "refresh-whitelist");
    }

    @Test
    public void TestFailedAgentFlow() {
        when(context.getRequest().getCredentials().getStatus())
                .thenReturn(CredentialsStatus.TEMPORARY_ERROR);
        when(context.getRequest().getProvider().getName()).thenReturn("ob-seb-test");
        when(context.getRequest().getProvider().getMarket()).thenReturn("SE");

        reporter.observe(context, "refresh-whitelist");
    }
}
