package se.tink.backend.aggregation.workers.commands;

import static java.lang.String.format;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.agents.utils.random.RandomUtils;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AggregationWorkerConfiguration;
import se.tink.backend.aggregation.logmasker.LogMasker.LoggingMode;
import se.tink.backend.aggregation.rpc.TransferRequest;
import se.tink.backend.aggregation.storage.logs.AgentHttpLogsSaver;
import se.tink.backend.aggregation.storage.logs.AgentHttpLogsSaverProvider;
import se.tink.backend.aggregation.storage.logs.AgentHttpLogsStorageHandler;
import se.tink.backend.aggregation.storage.logs.SaveLogsResult;
import se.tink.backend.aggregation.storage.logs.SaveLogsStatus;
import se.tink.backend.aggregation.storage.logs.handlers.AgentHttpLogsConstants.RawHttpLogsCatalog;
import se.tink.backend.aggregation.workers.commands.payment.PaymentsLegalConstraintsProvider;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsRequestType;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsStatus;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;
import se.tink.libraries.signableoperation.rpc.SignableOperation;
import se.tink.libraries.user.rpc.User;
import se.tink.libraries.uuid.UUIDUtils;

@Slf4j
public class DebugAgentWorkerCommand extends AgentWorkerCommand {

    private static final LogTag LOG_TAG = LogTag.from("[DebugAgentWorkerCommand]");
    private static final String TRYHARD_APPID = "addcb9c598fc4fef8497a64b142333e7";

    private final AgentWorkerCommandContext context;
    private final AgentHttpLogsStorageHandler logsStorageHandler;
    private final AgentHttpLogsSaverProvider logsSaverProvider;
    private final PaymentsLegalConstraintsProvider paymentsLegalConstraintsProvider;
    private final StringBuilder logResultsBuilder;

    private final AgentsServiceConfiguration agentsServiceConfiguration;
    private final Credentials credentials;
    private final User user;

    private AgentHttpLogsSaver logsSaver;

    public DebugAgentWorkerCommand(
            AgentWorkerCommandContext context, AgentHttpLogsStorageHandler logsStorageHandler) {
        this(
                context,
                logsStorageHandler,
                new AgentHttpLogsSaverProvider(),
                new PaymentsLegalConstraintsProvider(),
                new StringBuilder());
    }

    protected DebugAgentWorkerCommand(
            AgentWorkerCommandContext context,
            AgentHttpLogsStorageHandler logsStorageHandler,
            AgentHttpLogsSaverProvider logsSaverProvider,
            PaymentsLegalConstraintsProvider paymentsLegalConstraintsProvider,
            StringBuilder logResultsBuilder) {
        this.context = context;
        this.logsStorageHandler = logsStorageHandler;
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
        logsSaver = logsSaverProvider.createLogsSaver(context, logsStorageHandler);
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
            logResultsBuilder.append("\nSkipping all logs: disabled on cluster");
            return;
        }
        if (loggingMaskerMayNotCoverAllProvidersSecrets()) {
            logResultsBuilder.append(
                    "\nSkipping all logs: logging masker may not cover all secrets");
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
            logResultsBuilder.append("\nThis transfer request should not be logged");
            return;
        }

        String transferId = UUIDUtils.toTinkUUID(transferRequest.getTransfer().getId());
        String transferStatus =
                Optional.of(transferRequest.getSignableOperation())
                        .map(SignableOperation::getStatus)
                        .map(String::valueOf)
                        .orElse(null);
        SaveLogsResult rawLogsResult = logsSaver.saveRawLogs(RawHttpLogsCatalog.DEFAULT);
        if (rawLogsResult.isSaved()) {
            logResultsBuilder.append("\nPayment Transfer Status: ").append(transferStatus);
            logResultsBuilder
                    .append(
                            format(
                                    "%nFlushed transfer (%s) debug log for further investigation: ",
                                    transferId))
                    .append(rawLogsResult.getStorageDescription());
        } else {
            logSkippingRawLogs(rawLogsResult.getStatus());
        }

        if (shouldStoreInLongTermStorageForPaymentsDisputes(context.getAppId())) {
            SaveLogsResult ltsRawLogsResult =
                    logsSaver.saveRawLogs(RawHttpLogsCatalog.LTS_PAYMENTS);
            if (ltsRawLogsResult.isSaved()) {
                logResultsBuilder
                        .append(
                                format(
                                        "%nFlushed transfer to long term storage for payments disputes (%s) debug log for further investigation: ",
                                        transferId))
                        .append(ltsRawLogsResult.getStorageDescription());
            } else {
                logSkippingRawLtsLogs(ltsRawLogsResult.getStatus());
            }
        }

        SaveLogsResult jsonLogsResult = logsSaver.saveJsonLogs();
        if (jsonLogsResult.isSaved()) {
            logResultsBuilder
                    .append(format("%nFlushed transfer (%s) json logs: ", transferId))
                    .append(jsonLogsResult.getStorageDescription());
        } else {
            logSkippingJsonLogs(jsonLogsResult.getStatus());
        }

        if (shouldWriteHarLog()) {
            SaveLogsResult harLogsResult = logsSaver.saveHarLogs(RawHttpLogsCatalog.DEFAULT);
            if (harLogsResult.isSaved()) {
                logResultsBuilder
                        .append(format("%nFlushed transfer (%s) http archive: ", transferId))
                        .append(harLogsResult.getStorageDescription());
            } else {
                logSkippingHarLogs(harLogsResult.getStatus());
            }
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
            logResultsBuilder.append("\nThis request should not be logged");
            return;
        }

        SaveLogsResult rawLogsResult = logsSaver.saveRawLogs(RawHttpLogsCatalog.DEFAULT);
        if (rawLogsResult.isSaved()) {
            logResultsBuilder
                    .append("\nFlushed http logs: ")
                    .append(rawLogsResult.getStorageDescription());
        } else {
            logSkippingRawLogs(rawLogsResult.getStatus());
        }

        SaveLogsResult jsonLogsResult = logsSaver.saveJsonLogs();
        if (jsonLogsResult.isSaved()) {
            logResultsBuilder
                    .append("\nFlushed http json logs: ")
                    .append(jsonLogsResult.getStorageDescription());
        } else {
            logSkippingJsonLogs(jsonLogsResult.getStatus());
        }

        if (shouldWriteHarLog()) {
            SaveLogsResult harLogsResult = logsSaver.saveHarLogs(RawHttpLogsCatalog.DEFAULT);
            if (harLogsResult.isSaved()) {
                logResultsBuilder
                        .append("\nFlushed http archive: ")
                        .append(harLogsResult.getStorageDescription());
            } else {
                logSkippingHarLogs(harLogsResult.getStatus());
            }
        }
    }

    private boolean shouldWriteHarLog() {
        return getDebugLogHarFrequencyFromConfig() > RandomUtils.randomInt(100)
                || TRYHARD_APPID.equals(context.getAppId());
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

    private int getDebugLogHarFrequencyFromConfig() {
        return Optional.ofNullable(agentsServiceConfiguration)
                .map(AgentsServiceConfiguration::getAggregationWorker)
                .map(AggregationWorkerConfiguration::getDebugLogHarFrequencyPercent)
                .orElse(0);
    }

    private static void logSkippingRawLogs(SaveLogsStatus status) {
        log.warn("{} Skipping raw http logs: {}", LOG_TAG, status);
    }

    private static void logSkippingRawLtsLogs(SaveLogsStatus status) {
        log.warn("{} Skipping raw LTS http logs: {}", LOG_TAG, status);
    }

    private static void logSkippingJsonLogs(SaveLogsStatus status) {
        log.warn("{} Skipping JSON http logs: {}", LOG_TAG, status);
    }

    private static void logSkippingHarLogs(SaveLogsStatus status) {
        log.warn("{} Skipping har logs: {}", LOG_TAG, status);
    }
}
