package se.tink.backend.aggregation.workers.commands;

import static java.lang.String.format;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.utils.random.RandomUtils;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AggregationWorkerConfiguration;
import se.tink.backend.aggregation.logmasker.LogMasker.LoggingMode;
import se.tink.backend.aggregation.rpc.TransferRequest;
import se.tink.backend.aggregation.storage.logs.AgentDebugLogStorageHandler;
import se.tink.backend.aggregation.storage.logs.AgentDebugLogsSaver;
import se.tink.backend.aggregation.storage.logs.AgentDebugLogsSaverProvider;
import se.tink.backend.aggregation.storage.logs.SaveLogsResult;
import se.tink.backend.aggregation.storage.logs.handlers.AgentDebugLogConstants.AapLogsCatalog;
import se.tink.backend.aggregation.workers.commands.payment.PaymentsLegalConstraintsProvider;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsRequestType;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsStatus;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;
import se.tink.libraries.user.rpc.User;
import se.tink.libraries.uuid.UUIDUtils;

@Slf4j
public class DebugAgentWorkerCommand extends AgentWorkerCommand {

    private final AgentWorkerCommandContext context;
    private final AgentDebugLogStorageHandler logStorageHandler;
    private final AgentDebugLogsSaverProvider logsSaverProvider;
    private final PaymentsLegalConstraintsProvider paymentsLegalConstraintsProvider;
    private final StringBuilder logResultsBuilder;

    private final AgentsServiceConfiguration agentsServiceConfiguration;
    private final Credentials credentials;
    private final User user;

    private AgentDebugLogsSaver agentDebugLogsSaver;

    public DebugAgentWorkerCommand(
            AgentWorkerCommandContext context, AgentDebugLogStorageHandler logStorageHandler) {
        this(
                context,
                logStorageHandler,
                new AgentDebugLogsSaverProvider(),
                new PaymentsLegalConstraintsProvider(),
                new StringBuilder());
    }

    protected DebugAgentWorkerCommand(
            AgentWorkerCommandContext context,
            AgentDebugLogStorageHandler logStorageHandler,
            AgentDebugLogsSaverProvider logsSaverProvider,
            PaymentsLegalConstraintsProvider paymentsLegalConstraintsProvider,
            StringBuilder logResultsBuilder) {
        this.context = context;
        this.logStorageHandler = logStorageHandler;
        this.logsSaverProvider = logsSaverProvider;
        this.paymentsLegalConstraintsProvider = paymentsLegalConstraintsProvider;
        this.logResultsBuilder = logResultsBuilder;

        this.agentsServiceConfiguration = context.getAgentsServiceConfiguration();
        this.credentials = context.getRequest().getCredentials();
        this.user = context.getRequest().getUser();
    }

    @Override
    protected AgentWorkerCommandResult doExecute() {
        if (Objects.isNull(context.getAgentConfigurationController())) {
            throw new IllegalStateException(
                    "`CreateAgentConfigurationControllerWorkerCommand` was not executed before `DebugAgentWorkerCommand`.");
        }
        return AgentWorkerCommandResult.CONTINUE;
    }

    @Override
    protected void doPostProcess() {
        agentDebugLogsSaver = logsSaverProvider.createLogsSaver(context, logStorageHandler);
        tryToSaveLogs();
        log.info(logResultsBuilder.toString());
    }

    private void tryToSaveLogs() {
        boolean isTransferRequest =
                context.getRequest().getType() == CredentialsRequestType.TRANSFER;
        if (isTransferRequest) {
            logResultsBuilder
                    .append("Payment Credentials Status: ")
                    .append(credentials.getStatus());
        } else {
            logResultsBuilder.append("Credential Status: ").append(credentials.getStatus());
        }

        if (isLoggingDisabledOnCluster()) {
            logResultsBuilder.append("\nSkipping logs: disabled on cluster");
            return;
        }
        if (loggingMaskerMayNotCoverAllProvidersSecrets()) {
            logResultsBuilder.append("\nSkipping logs: logging masker may not cover all secrets");
            return;
        }

        if (isTransferRequest) {
            handleLoggingForTransferRequest((TransferRequest) context.getRequest());
        } else {
            handleLoggingForCommonRequest();
        }
    }

    private void handleLoggingForTransferRequest(TransferRequest transferRequest) {
        if (!shouldLogTransferRequest(transferRequest)) {
            logResultsBuilder.append("\nSkipping logs: should not log");
            return;
        }

        String transferId = UUIDUtils.toTinkUUID(transferRequest.getTransfer().getId());

        SaveLogsResult aapLogsResult = agentDebugLogsSaver.saveAapLogs(AapLogsCatalog.DEFAULT);
        if (aapLogsResult.isSaved()) {
            logResultsBuilder
                    .append(
                            format(
                                    "%nFlushed transfer (%s) debug log for further investigation: ",
                                    transferId))
                    .append(aapLogsResult.getStorageDescription());
        }

        if (shouldStoreInLongTermStorageForPaymentsDisputes(context.getAppId())) {
            SaveLogsResult ltsAapLogsResult =
                    agentDebugLogsSaver.saveAapLogs(AapLogsCatalog.LTS_PAYMENTS);
            if (ltsAapLogsResult.isSaved()) {
                logResultsBuilder
                        .append(
                                format(
                                        "%nFlushed transfer to long term storage for payments disputes (%s) debug log for further investigation: ",
                                        transferId))
                        .append(ltsAapLogsResult.getStorageDescription());
            }
        }

        SaveLogsResult jsonLogsResult = agentDebugLogsSaver.saveJsonLogs();
        if (jsonLogsResult.isSaved()) {
            logResultsBuilder
                    .append(format("%nFlushed transfer (%s) json logs: ", transferId))
                    .append(jsonLogsResult.getStorageDescription());
        }
    }

    private boolean shouldLogTransferRequest(TransferRequest transferRequest) {
        SignableOperationStatuses transferStatus =
                transferRequest.getSignableOperation().getStatus();

        boolean transferStatusRequiresLogging =
                transferStatus == SignableOperationStatuses.FAILED
                        || transferStatus == SignableOperationStatuses.CANCELLED;

        boolean isDebugForced = shouldSaveDebugLogRegardless();

        return transferStatusRequiresLogging || isDebugForced;
    }

    private boolean shouldStoreInLongTermStorageForPaymentsDisputes(String appId) {
        return paymentsLegalConstraintsProvider.getForAppId(appId).isOnTinksLicense();
    }

    private void handleLoggingForCommonRequest() {
        if (!shouldLogCommonRequest()) {
            logResultsBuilder.append("\nSkipping logs: should not log");
            return;
        }

        SaveLogsResult aapLogsResult = agentDebugLogsSaver.saveAapLogs(AapLogsCatalog.DEFAULT);
        if (aapLogsResult.isSaved()) {
            logResultsBuilder
                    .append("\nFlushed http logs: ")
                    .append(aapLogsResult.getStorageDescription());
        }

        SaveLogsResult jsonLogsResult = agentDebugLogsSaver.saveJsonLogs();
        if (jsonLogsResult.isSaved()) {
            logResultsBuilder
                    .append("\nFlushed http json logs: ")
                    .append(jsonLogsResult.getStorageDescription());
        }
    }

    private boolean shouldLogCommonRequest() {
        boolean credentialsStatusRequiresLogging =
                credentials.getStatus() == CredentialsStatus.AUTHENTICATION_ERROR
                        || credentials.getStatus() == CredentialsStatus.TEMPORARY_ERROR
                        || credentials.getStatus() == CredentialsStatus.UNCHANGED;

        boolean isDebugForced =
                credentials.isDebug() || user.isDebug() || shouldSaveDebugLogRegardless();

        return credentialsStatusRequiresLogging || isDebugForced;
    }

    private boolean isLoggingDisabledOnCluster() {
        String clusterId = context.getClusterId();
        List<String> excludedDebugClusters =
                context.getAgentsServiceConfiguration()
                        .getExcludedDebugClusters()
                        .getExcludedClusters();
        return Objects.nonNull(excludedDebugClusters) && excludedDebugClusters.contains(clusterId);
    }

    private boolean loggingMaskerMayNotCoverAllProvidersSecrets() {
        LoggingMode providerLoggingMode =
                context.getLogMasker().shouldLog(context.getRequest().getProvider());
        return providerLoggingMode != LoggingMode.LOGGING_MASKER_COVERS_SECRETS;
    }

    private boolean shouldSaveDebugLogRegardless() {
        return getDebugLogFrequencyFromConfig() > RandomUtils.randomInt(100);
    }

    private int getDebugLogFrequencyFromConfig() {
        return Optional.ofNullable(agentsServiceConfiguration)
                .map(AgentsServiceConfiguration::getAggregationWorker)
                .map(AggregationWorkerConfiguration::getDebugLogFrequencyPercent)
                .orElse(0);
    }
}
