package se.tink.backend.aggregation.workers.commands;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.utils.random.RandomUtils;
import se.tink.backend.aggregation.logmasker.LogMasker;
import se.tink.backend.aggregation.logmasker.LogMaskerImpl;
import se.tink.backend.aggregation.logmasker.LogMaskerImpl.LoggingMode;
import se.tink.backend.aggregation.rpc.TransferRequest;
import se.tink.backend.aggregation.storage.debug.AgentDebugStorageHandler;
import se.tink.backend.aggregation.workers.commands.exception.EmptyDebugLogException;
import se.tink.backend.aggregation.workers.commands.state.DebugAgentWorkerCommandState;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsRequestType;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsStatus;
import se.tink.libraries.date.ThreadSafeDateFormat;
import se.tink.libraries.payments_legal_constraints.PaymentsLegalConstraints;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;
import se.tink.libraries.user.rpc.User;
import se.tink.libraries.uuid.UUIDUtils;

public class DebugAgentWorkerCommand extends AgentWorkerCommand {

    private static final Logger log = LoggerFactory.getLogger(DebugAgentWorkerCommand.class);

    private DebugAgentWorkerCommandState state;
    private AgentWorkerCommandContext context;
    private AgentDebugStorageHandler agentDebugStorage;
    private LogMasker logMasker;

    public DebugAgentWorkerCommand(
            AgentWorkerCommandContext context,
            DebugAgentWorkerCommandState state,
            AgentDebugStorageHandler agentDebugStorage) {
        this.context = context;
        this.state = state;
        this.agentDebugStorage = agentDebugStorage;
    }

    @Override
    protected AgentWorkerCommandResult doExecute() {
        if (Objects.isNull(context.getAgentConfigurationController())) {
            throw new IllegalStateException(
                    "`CreateAgentConfigurationControllerWorkerCommand` was not executed before `DebugAgentWorkerCommand`.");
        }
        this.logMasker = context.getLogMasker();
        return AgentWorkerCommandResult.CONTINUE;
    }

    @Override
    protected void doPostProcess() {

        // Disable logging depending on this.
        if (LoggingMode.UNSURE_IF_MASKER_COVERS_SECRETS.equals(
                LogMaskerImpl.shouldLog(context.getRequest().getProvider()))) {
            return;
        }

        if (!agentDebugStorage.isEnabled()) {
            return;
        }

        String clusterId = context.getClusterId();
        List<String> excludedDebugClusters =
                context.getAgentsServiceConfiguration()
                        .getExcludedDebugClusters()
                        .getExcludedClusters();

        if (Objects.nonNull(excludedDebugClusters) && excludedDebugClusters.contains(clusterId)) {
            return;
        }

        Credentials credentials = context.getRequest().getCredentials();

        if (context.getRequest().getType() == CredentialsRequestType.TRANSFER) {
            TransferRequest transferRequest = (TransferRequest) context.getRequest();

            // Debug output for transfers.
            SignableOperationStatuses transferStatus =
                    transferRequest.getSignableOperation().getStatus();
            if (transferStatus == SignableOperationStatuses.FAILED
                    || transferStatus == SignableOperationStatuses.CANCELLED
                    || shouldPrintDebugLogRegardless()) {
                writeToDebugFile(credentials, transferRequest);
            }
        } else {
            User user = context.getRequest().getUser();

            // Debug output for non-transfers such as refresh commands and delete.
            if (shouldPrintDebugLog(credentials, user)) {
                writeToDebugFile(credentials, null);
            }
        }
    }

    private boolean shouldPrintDebugLog(Credentials credentials, User user) {
        return credentials.getStatus() == CredentialsStatus.AUTHENTICATION_ERROR
                || credentials.getStatus() == CredentialsStatus.TEMPORARY_ERROR
                || credentials.getStatus() == CredentialsStatus.UNCHANGED
                || credentials.isDebug()
                || user.isDebug()
                || shouldPrintDebugLogRegardless();
    }

    private String maskSensitiveOutputLog(String logContent, Credentials credentials) {
        for (Field providerField : context.getRequest().getProvider().getFields()) {
            String credentialFieldValue = credentials.getField(providerField.getName());

            if (Objects.nonNull(credentialFieldValue)) {
                logContent =
                        logContent.replace(
                                credentialFieldValue, "***" + providerField.getName() + "***");
            }
        }

        // If we have no masker, log nothing.
        if (Objects.isNull(logMasker)) {
            return "";
        }
        return logMasker.mask(logContent);
    }

    private static String getFormattedSize(String str) {
        int lines = str.split("\n").length;
        int bytesUtf8 = str.getBytes(StandardCharsets.UTF_8).length;
        return String.format("%dB_%d", bytesUtf8, lines);
    }

    private void writeToDebugFile(Credentials credentials, TransferRequest transferRequest) {
        try {
            File logFile = null;
            String logContent = getCleanLogContent(credentials);
            if (agentDebugStorage.isLocalStorage() && state.isSaveLocally()) {
                logFile =
                        new File(
                                state.getDebugDirectory(),
                                getFormattedFileName(logContent, credentials, false));
            }

            if (!agentDebugStorage.isLocalStorage()) {
                logFile = new File(getFormattedFileName(logContent, credentials, false));
            }

            if (Objects.isNull(logFile)) {
                log.warn(
                        "Created debug log but local storage cannot be used & no S3 storage configuration available.");
                return;
            }

            if (shouldStoreInLongTermStorageForPaymentsDisputes(context.getAppId())) {
                writeToPaymentsLongTermDisputeStorage(logContent, credentials, transferRequest);
            }

            String storagePath = agentDebugStorage.store(logContent, logFile);

            if (transferRequest != null) {
                String id = UUIDUtils.toTinkUUID(transferRequest.getTransfer().getId());
                log.info(
                        "Flushed transfer ("
                                + id
                                + ") debug log for further investigation: "
                                + storagePath);

            } else {
                log.info(
                        "Credential Status: {} \nFlushed http log: {}",
                        credentials.getStatus(),
                        storagePath);
            }

        } catch (EmptyDebugLogException e) {
            log.info(e.getMessage());
        } catch (IOException e) {
            log.error("Could not write debug logFile.", e);
        }
    }

    private void writeToPaymentsLongTermDisputeStorage(
            String logContent, Credentials credentials, TransferRequest transferRequest) {
        try {
            File ltsLogFile = new File(getFormattedFileName(logContent, credentials, true));
            String ltsStoragePath = agentDebugStorage.store(logContent, ltsLogFile);
            log.info(
                    "Flushed transfer to long term storage for payments disputes ("
                            + UUIDUtils.toTinkUUID(transferRequest.getTransfer().getId())
                            + ") debug log for further investigation: "
                            + ltsStoragePath);
        } catch (IOException e) {
            log.error("Could not write debug log file to payments dispute long term storage.", e);
        }
    }

    private boolean shouldStoreInLongTermStorageForPaymentsDisputes(String appId) {
        PaymentsLegalConstraints legalConstraints = PaymentsLegalConstraints.get(appId);
        return legalConstraints.isOnTinksLicense();
    }

    private String getCleanLogContent(Credentials credentials)
            throws UnsupportedEncodingException, EmptyDebugLogException {
        String logContent = context.getLogOutputStream().toString(StandardCharsets.UTF_8.name());

        // Don't save logs without content
        if (logContent.getBytes(StandardCharsets.UTF_8).length == 0) {
            throw new EmptyDebugLogException();
        }

        return maskSensitiveOutputLog(logContent, credentials);
    }

    private String getFormattedFileName(
            String logContent, Credentials credentials, boolean isLtsPaymentsDisputeFile) {
        LocalDateTime now = LocalDateTime.now();
        String disputePrefix = "";
        if (isLtsPaymentsDisputeFile) {
            disputePrefix =
                    String.format(
                            "%s/%s/%s/%s/",
                            state.getLongTermStorageDisputeBasePrefixFromConfig(),
                            now.getYear(),
                            now.getMonthValue(),
                            now.getDayOfMonth());
        }
        return String.format(
                        "%s%s_%s_u%s_c%s_%s.log",
                        disputePrefix,
                        credentials.getProviderName(),
                        ThreadSafeDateFormat.FORMATTER_FILENAME_SAFE.format(
                                now.atZone(ZoneId.systemDefault()).toInstant()),
                        credentials.getUserId(),
                        credentials.getId(),
                        getFormattedSize(logContent))
                .replace(":", ".");
    }

    private boolean shouldPrintDebugLogRegardless() {
        return state.getDebugFrequencyPercent() > RandomUtils.randomInt(100);
    }
}
