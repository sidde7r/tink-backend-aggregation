package se.tink.backend.aggregation.storage.logs;

import com.google.inject.Guice;
import com.google.inject.Injector;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;

public class AgentHttpLogsSaverProvider {

    public AgentHttpLogsSaver createLogsSaver(
            AgentWorkerCommandContext commandContext,
            AgentHttpLogsStorageHandler logStorageHandler) {

        AgentHttpLogsSaverModule module =
                new AgentHttpLogsSaverModule(commandContext, logStorageHandler);
        Injector injector = Guice.createInjector(module);
        return injector.getInstance(AgentHttpLogsSaver.class);
    }
}
