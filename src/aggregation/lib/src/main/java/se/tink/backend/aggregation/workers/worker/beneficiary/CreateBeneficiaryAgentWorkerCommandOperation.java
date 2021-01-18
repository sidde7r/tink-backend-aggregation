package se.tink.backend.aggregation.workers.worker.beneficiary;

import com.google.common.collect.Lists;
import java.util.List;
import org.apache.curator.framework.CuratorFramework;
import se.tink.backend.agents.rpc.CredentialsStatus;
import se.tink.backend.aggregation.aggregationcontroller.ControllerWrapper;
import se.tink.backend.aggregation.api.AggregatorInfo;
import se.tink.backend.aggregation.cluster.identification.ClientInfo;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.models.ProviderTierConfiguration;
import se.tink.backend.aggregation.controllers.ProviderSessionCacheController;
import se.tink.backend.aggregation.controllers.SupplementalInformationController;
import se.tink.backend.aggregation.events.AccountInformationServiceEventsProducer;
import se.tink.backend.aggregation.events.LoginAgentEventProducer;
import se.tink.backend.aggregation.rpc.CreateBeneficiaryCredentialsRequest;
import se.tink.backend.aggregation.storage.debug.AgentDebugStorageHandler;
import se.tink.backend.aggregation.workers.agent_metrics.AgentWorkerMetricReporter;
import se.tink.backend.aggregation.workers.commands.CircuitBreakerAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.ClearSensitivePayloadOnForceAuthenticateCommand;
import se.tink.backend.aggregation.workers.commands.CreateAgentConfigurationControllerWorkerCommand;
import se.tink.backend.aggregation.workers.commands.CreateBeneficiaryAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.CreateLogMaskerWorkerCommand;
import se.tink.backend.aggregation.workers.commands.DebugAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.DecryptCredentialsWorkerCommand;
import se.tink.backend.aggregation.workers.commands.ExpireSessionAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.InstantiateAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.LockAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.LoginAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.MigrateCredentialsAndAccountsWorkerCommand;
import se.tink.backend.aggregation.workers.commands.ReportProviderMetricsAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.SetCredentialsStatusAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.UpdateCredentialsStatusAgentWorkerCommand;
import se.tink.backend.aggregation.workers.commands.ValidateProviderAgentWorkerStatus;
import se.tink.backend.aggregation.workers.commands.state.CircuitBreakerAgentWorkerCommandState;
import se.tink.backend.aggregation.workers.commands.state.DebugAgentWorkerCommandState;
import se.tink.backend.aggregation.workers.commands.state.InstantiateAgentWorkerCommandState;
import se.tink.backend.aggregation.workers.commands.state.LoginAgentWorkerCommandState;
import se.tink.backend.aggregation.workers.commands.state.ReportProviderMetricsAgentWorkerCommandState;
import se.tink.backend.aggregation.workers.concurrency.InterProcessSemaphoreMutexFactory;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.encryption.CredentialsCrypto;
import se.tink.backend.aggregation.workers.metrics.AgentWorkerCommandMetricState;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.operation.AgentWorkerOperation;
import se.tink.backend.aggregation.workers.operation.AgentWorkerOperation.AgentWorkerOperationState;
import se.tink.backend.aggregation.wrappers.CryptoWrapper;
import se.tink.backend.integration.tpp_secrets_service.client.iface.TppSecretsServiceClient;
import se.tink.libraries.cache.CacheClient;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.metrics.registry.MetricRegistry;

public class CreateBeneficiaryAgentWorkerCommandOperation {

    // States
    private static MetricRegistry metricRegistry;
    private static LoginAgentEventProducer loginAgentEventProducer;
    private static LoginAgentWorkerCommandState loginAgentWorkerCommandState;

    public static AgentWorkerOperation createOperationCreateBeneficiary(
            CreateBeneficiaryCredentialsRequest request,
            ClientInfo clientInfo,
            MetricRegistry metricRegistry,
            CuratorFramework coordinationClient,
            ControllerWrapper controllerWrapper,
            AgentsServiceConfiguration agentsServiceConfiguration,
            AggregatorInfo aggregatorInfo,
            SupplementalInformationController supplementalInformationController,
            ProviderSessionCacheController providerSessionCacheController,
            String correlationId,
            CryptoWrapper cryptoWrapper,
            CircuitBreakerAgentWorkerCommandState circuitBreakAgentWorkerCommandState,
            InterProcessSemaphoreMutexFactory interProcessSemaphoreMutexFactory,
            CacheClient cacheClient,
            ReportProviderMetricsAgentWorkerCommandState reportMetricsAgentWorkerCommandState,
            TppSecretsServiceClient tppSecretsServiceClient,
            DebugAgentWorkerCommandState debugAgentWorkerCommandState,
            AgentDebugStorageHandler agentDebugStorageHandler,
            InstantiateAgentWorkerCommandState instantiateAgentWorkerCommandState,
            LoginAgentWorkerCommandState loginAgentWorkerCommandState,
            LoginAgentEventProducer loginAgentEventProducer,
            AgentWorkerOperationState agentWorkerOperationState,
            ProviderTierConfiguration providerTierConfiguration,
            AccountInformationServiceEventsProducer accountInformationServiceEventsProducer) {

        CreateBeneficiaryAgentWorkerCommandOperation.metricRegistry = metricRegistry;
        CreateBeneficiaryAgentWorkerCommandOperation.loginAgentEventProducer =
                loginAgentEventProducer;
        CreateBeneficiaryAgentWorkerCommandOperation.loginAgentWorkerCommandState =
                loginAgentWorkerCommandState;

        AgentWorkerCommandContext context =
                new AgentWorkerCommandContext(
                        request,
                        metricRegistry,
                        coordinationClient,
                        agentsServiceConfiguration,
                        aggregatorInfo,
                        supplementalInformationController,
                        providerSessionCacheController,
                        controllerWrapper,
                        clientInfo.getClusterId(),
                        clientInfo.getAppId(),
                        correlationId,
                        accountInformationServiceEventsProducer);

        List<AgentWorkerCommand> commands = Lists.newArrayList();
        String metricsName = "create-beneficiary";
        // TODO: Implement and add add beneficiary event trigger command here.

        // NOTE: Please be aware that the order of adding commands is meaningful
        commands.add(new ValidateProviderAgentWorkerStatus(context, controllerWrapper));
        commands.add(
                new ExpireSessionAgentWorkerCommand(
                        request.isManual(),
                        context,
                        request.getCredentials(),
                        request.getProvider()));
        commands.add(
                new CircuitBreakerAgentWorkerCommand(context, circuitBreakAgentWorkerCommandState));
        commands.add(
                new LockAgentWorkerCommand(context, metricsName, interProcessSemaphoreMutexFactory)
                        .withLoginEvent(loginAgentEventProducer));
        commands.add(
                new DecryptCredentialsWorkerCommand(
                        context,
                        new CredentialsCrypto(
                                cacheClient, controllerWrapper, cryptoWrapper, metricRegistry)));
        commands.add(
                new MigrateCredentialsAndAccountsWorkerCommand(
                        context.getRequest(), controllerWrapper, clientInfo));
        commands.add(
                new UpdateCredentialsStatusAgentWorkerCommand(
                        controllerWrapper,
                        request.getCredentials(),
                        request.getProvider(),
                        context,
                        agentContext ->
                                !agentContext.isWaitingOnConnectorTransactions()
                                        && !agentContext.isSystemProcessingTransactions()));
        commands.add(
                new ReportProviderMetricsAgentWorkerCommand(
                        context,
                        metricsName,
                        reportMetricsAgentWorkerCommandState,
                        new AgentWorkerMetricReporter(metricRegistry, providerTierConfiguration)));
        commands.add(
                new CreateAgentConfigurationControllerWorkerCommand(
                        context, tppSecretsServiceClient));
        commands.add(new CreateLogMaskerWorkerCommand(context));
        commands.add(
                new DebugAgentWorkerCommand(
                        context, debugAgentWorkerCommandState, agentDebugStorageHandler));
        commands.add(
                new InstantiateAgentWorkerCommand(context, instantiateAgentWorkerCommandState));

        addClearSensitivePayloadOnForceAuthenticateCommandAndLoginAgentWorkerCommand(
                commands, context);

        commands.add(
                new SetCredentialsStatusAgentWorkerCommand(context, CredentialsStatus.UPDATING));
        commands.add(
                new CreateBeneficiaryAgentWorkerCommand(
                        context, request, createCommandMetricState(request, metricRegistry)));
        return new AgentWorkerOperation(
                agentWorkerOperationState, metricsName, request, commands, context);
    }

    private static AgentWorkerCommandMetricState createCommandMetricState(
            CredentialsRequest request, MetricRegistry metricRegistry) {
        return new AgentWorkerCommandMetricState(
                request.getProvider(), request.getCredentials(), metricRegistry, request.getType());
    }

    private static void
            addClearSensitivePayloadOnForceAuthenticateCommandAndLoginAgentWorkerCommand(
                    List<AgentWorkerCommand> commands, AgentWorkerCommandContext context) {

        /* LoginAgentWorkerCommand needs to always be used together with ClearSensitivePayloadOnForceAuthenticateCommand */

        commands.add(new ClearSensitivePayloadOnForceAuthenticateCommand(context));
        commands.add(
                new LoginAgentWorkerCommand(
                        context,
                        loginAgentWorkerCommandState,
                        createCommandMetricState(context.getRequest(), metricRegistry),
                        metricRegistry,
                        loginAgentEventProducer));
    }
}
