package se.tink.backend.aggregation.workers.commands.exceptions.handlers;

import static org.mockito.Mockito.mock;

import org.junit.Ignore;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.metrics.MetricAction;
import se.tink.libraries.i18n_aggregation.Catalog;

@Ignore
public abstract class ExceptionHandlerBaseTest {

    protected ExceptionHandler handler;
    protected MetricAction metricAction;
    protected AgentWorkerCommandContext context;
    protected Catalog catalog;

    public void setUp() {
        metricAction = mock(MetricAction.class);
        context = mock(AgentWorkerCommandContext.class);
        catalog = mock(Catalog.class);
    }
}
