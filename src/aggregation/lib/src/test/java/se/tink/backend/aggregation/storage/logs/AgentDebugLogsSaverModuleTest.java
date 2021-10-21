package se.tink.backend.aggregation.storage.logs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.Test;
import org.mockito.Answers;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;

public class AgentDebugLogsSaverModuleTest {

    @Test
    public void should_inject_all_dependencies() {
        // given
        AgentWorkerCommandContext commandContext =
                mock(AgentWorkerCommandContext.class, Answers.RETURNS_DEEP_STUBS);
        when(commandContext.getOperationName()).thenReturn("operationName");
        when(commandContext.getAppId()).thenReturn("appId");
        AgentDebugLogStorageHandler logStorageHandler = mock(AgentDebugLogStorageHandler.class);

        AgentDebugLogsSaverModule module =
                new AgentDebugLogsSaverModule(commandContext, logStorageHandler);
        Injector injector = Guice.createInjector(module);

        // when
        AgentDebugLogsSaver logsSaver = injector.getInstance(AgentDebugLogsSaver.class);

        // then
        assertThat(logsSaver).isNotNull();
    }
}
