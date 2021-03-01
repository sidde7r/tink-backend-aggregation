package se.tink.backend.aggregation.workers.commands;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.Sets;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.instrumentation.FetcherInstrumentationRegistry;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.metrics.collection.MetricCollector;
import se.tink.libraries.metrics.registry.MetricRegistry;

public class FetcherInstrumentationRegistryAgentWorkerCommandTest {

    private FetcherInstrumentationAgentWorkerCommand command;
    private AgentWorkerCommandContext context;
    private NextGenerationAgent agent;
    private Provider provider;
    private MetricCollector metricCollector;
    private FetcherInstrumentationRegistry instrumentation;
    private MetricRegistry metricRegistry;

    @Before
    public void setUp() {
        metricCollector = new MetricCollector();
        metricRegistry = new MetricRegistry(metricCollector);
        provider = createProvider();
        context = mock(AgentWorkerCommandContext.class, Answers.RETURNS_DEEP_STUBS);
        agent = mock(NextGenerationAgent.class);

        CredentialsRequest request = mock(CredentialsRequest.class);

        when(context.getAgent()).thenReturn(agent);
        when(context.getRequest()).thenReturn(request);
        when(request.getProvider()).thenReturn(provider);
        when(context.getMetricRegistry()).thenReturn(metricRegistry);

        instrumentation = new FetcherInstrumentationRegistry();
        instrumentation.personal(AccountTypes.CHECKING, 2);
        instrumentation.personal(AccountTypes.CREDIT_CARD, 2);
        instrumentation.business(AccountTypes.CHECKING, 1);
    }

    @Test
    public void whenAgentIsNotNextGenCommandShouldReturnContinue() throws Exception {
        command = new FetcherInstrumentationAgentWorkerCommand(context, Sets.newHashSet());
        when(context.getAgent()).thenReturn(new FakeAgent());

        AgentWorkerCommandResult result = command.doExecute();
        assertThat(result).isEqualTo(AgentWorkerCommandResult.CONTINUE);
    }

    @Test
    public void whenAgentReturnsEmptyInstrumentationCommandShouldReturnContinue() throws Exception {
        command = new FetcherInstrumentationAgentWorkerCommand(context, Sets.newHashSet());
        when(agent.getFetcherInstrumentation()).thenReturn(Optional.empty());

        AgentWorkerCommandResult result = command.doExecute();
        assertThat(result).isEqualTo(AgentWorkerCommandResult.CONTINUE);
    }

    @Test
    public void whenAgentReturnsInstrumentationCommandShouldReturnContinue() throws Exception {
        command = new FetcherInstrumentationAgentWorkerCommand(context, Sets.newHashSet());
        when(agent.getFetcherInstrumentation()).thenReturn(Optional.of(instrumentation));

        AgentWorkerCommandResult result = command.doExecute();
        assertThat(result).isEqualTo(AgentWorkerCommandResult.CONTINUE);
    }

    private static class FakeAgent implements Agent {
        @Override
        public void setConfiguration(AgentsServiceConfiguration configuration) {}

        @Override
        public Class<? extends Agent> getAgentClass() {
            return null;
        }

        @Override
        public boolean login() throws Exception {
            return false;
        }

        @Override
        public void logout() throws Exception {}

        @Override
        public void close() {}
    }

    private static Provider createProvider() {
        Provider p = new Provider();
        p.setName("provider");
        p.setMarket("GB");
        return p;
    }
}
