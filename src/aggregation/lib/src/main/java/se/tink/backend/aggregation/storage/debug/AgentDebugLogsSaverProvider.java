package se.tink.backend.aggregation.storage.debug;

import com.google.inject.Guice;
import com.google.inject.Injector;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;

public class AgentDebugLogsSaverProvider {

    public AgentDebugLogsSaver createLogsSaver(
            AgentWorkerCommandContext commandContext,
            AgentDebugLogStorageHandler logStorageHandler) {

        AgentDebugLogsSaverModule module =
                new AgentDebugLogsSaverModule(commandContext, logStorageHandler);
        Injector injector = Guice.createInjector(module);
        return injector.getInstance(AgentDebugLogsSaver.class);
    }
}
