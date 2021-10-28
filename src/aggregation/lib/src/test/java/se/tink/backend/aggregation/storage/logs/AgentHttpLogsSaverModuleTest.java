package se.tink.backend.aggregation.storage.logs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.Test;
import org.mockito.Answers;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;

public class AgentHttpLogsSaverModuleTest {

    @Test
    public void should_inject_all_dependencies() {
        // given
        AgentWorkerCommandContext commandContext =
                mock(AgentWorkerCommandContext.class, Answers.RETURNS_DEEP_STUBS);
        when(commandContext.getOperationName()).thenReturn("operationName");
        when(commandContext.getAppId()).thenReturn("appId");
        AgentHttpLogsStorageHandler logStorageHandler = mock(AgentHttpLogsStorageHandler.class);

        AgentHttpLogsSaverModule module =
                new AgentHttpLogsSaverModule(commandContext, logStorageHandler);
        Injector injector = Guice.createInjector(module);

        // when
        AgentHttpLogsSaver logsSaver = injector.getInstance(AgentHttpLogsSaver.class);

        // then
        assertThat(logsSaver).isNotNull();
    }
}
