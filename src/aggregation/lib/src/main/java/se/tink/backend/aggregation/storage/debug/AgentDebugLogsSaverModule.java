package se.tink.backend.aggregation.storage.debug;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import lombok.RequiredArgsConstructor;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.logmasker.LogMasker;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.ActualLocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.http.log.executor.aap.HttpAapLogger;
import se.tink.backend.aggregation.nxgen.http.log.executor.json.HttpJsonLogger;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;

@RequiredArgsConstructor
public class AgentDebugLogsSaverModule extends AbstractModule {

    private final AgentWorkerCommandContext commandContext;
    private final AgentDebugLogStorageHandler logStorageHandler;

    @Override
    protected void configure() {
        bind(AgentsServiceConfiguration.class)
                .toInstance(commandContext.getAgentsServiceConfiguration());
        bind(LogMasker.class).toInstance(commandContext.getLogMasker());
        bind(HttpAapLogger.class).toInstance(commandContext.getHttpAapLogger());
        bind(HttpJsonLogger.class).toInstance(commandContext.getHttpJsonLogger());
        bind(Credentials.class).toInstance(commandContext.getRequest().getCredentials());
        bind(Provider.class).toInstance(commandContext.getRequest().getProvider());
        bindConstant()
                .annotatedWith(Names.named("operationName"))
                .to(commandContext.getOperationName());
        bindConstant().annotatedWith(Names.named("appId")).to(commandContext.getAppId());

        bind(AgentDebugLogStorageHandler.class).toInstance(logStorageHandler);

        bind(LocalDateTimeSource.class).toInstance(new ActualLocalDateTimeSource());
    }
}
