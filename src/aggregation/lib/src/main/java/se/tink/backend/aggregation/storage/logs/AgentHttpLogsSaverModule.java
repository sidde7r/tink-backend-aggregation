package se.tink.backend.aggregation.storage.logs;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import lombok.RequiredArgsConstructor;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.logmasker.LogMasker;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.ActualLocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.http.log.executor.json.JsonHttpTrafficLogger;
import se.tink.backend.aggregation.nxgen.http.log.executor.raw.RawHttpTrafficLogger;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.libraries.se.tink.libraries.har_logger.src.logger.HarLogCollector;

@RequiredArgsConstructor
public class AgentHttpLogsSaverModule extends AbstractModule {

    private final AgentWorkerCommandContext commandContext;
    private final AgentHttpLogsStorageHandler logsStorageHandler;

    @Override
    protected void configure() {
        bind(AgentsServiceConfiguration.class)
                .toInstance(commandContext.getAgentsServiceConfiguration());
        bind(LogMasker.class).toInstance(commandContext.getLogMasker());
        bind(RawHttpTrafficLogger.class).toInstance(commandContext.getRawHttpTrafficLogger());
        bind(JsonHttpTrafficLogger.class).toInstance(commandContext.getJsonHttpTrafficLogger());
        bind(HarLogCollector.class).toInstance(commandContext.getHarLogCollector());
        bind(Credentials.class).toInstance(commandContext.getRequest().getCredentials());
        bind(Provider.class).toInstance(commandContext.getRequest().getProvider());
        bindConstant()
                .annotatedWith(Names.named("operationName"))
                .to(commandContext.getOperationName());
        bindConstant().annotatedWith(Names.named("appId")).to(commandContext.getAppId());

        bind(AgentHttpLogsStorageHandler.class).toInstance(logsStorageHandler);

        bind(LocalDateTimeSource.class).toInstance(new ActualLocalDateTimeSource());
    }
}
