package se.tink.backend.aggregation.workers.commands;

import com.google.common.collect.ImmutableList;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsStatus;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.utils.random.RandomUtils;
import se.tink.backend.aggregation.log.LogMasker;
import se.tink.backend.aggregation.log.LogMasker.LoggingMode;
import se.tink.backend.aggregation.rpc.TransferRequest;
import se.tink.backend.aggregation.storage.debug.AgentDebugStorageHandler;
import se.tink.backend.aggregation.utils.ClientConfigurationStringMaskerBuilder;
import se.tink.backend.aggregation.utils.CredentialsStringMaskerBuilder;
import se.tink.backend.aggregation.workers.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.AgentWorkerCommandResult;
import se.tink.backend.aggregation.workers.commands.exception.EmptyDebugLogException;
import se.tink.backend.aggregation.workers.commands.state.DebugAgentWorkerCommandState;
import se.tink.libraries.credentials.service.CredentialsRequestType;
import se.tink.libraries.date.ThreadSafeDateFormat;
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
    public AgentWorkerCommandResult execute() {

        return AgentWorkerCommandResult.CONTINUE;
    }

    @Override
    public void postProcess() {

        // Disable logging depending on this.
        if (LoggingMode.UNSURE_IF_MASKER_COVERS_SECRETS.equals(
                LogMasker.shouldLog(context.getRequest().getProvider()))) {
            return;
        }

        if (!agentDebugStorage.isEnabled()) {
            return;
        }

        this.logMasker =
                LogMasker.builder()
                        .addStringMaskerBuilder(
                                new CredentialsStringMaskerBuilder(
                                        context.getRequest().getCredentials(),
                                        ImmutableList.of(
                                                CredentialsStringMaskerBuilder.CredentialsProperty
                                                        .PASSWORD,
                                                CredentialsStringMaskerBuilder.CredentialsProperty
                                                        .SECRET_KEY,
                                                CredentialsStringMaskerBuilder.CredentialsProperty
                                                        .SENSITIVE_PAYLOAD,
                                                CredentialsStringMaskerBuilder.CredentialsProperty
                                                        .USERNAME)))
                        .addStringMaskerBuilder(
                                new ClientConfigurationStringMaskerBuilder(
                                        context.getAgentConfigurationController()
                                                .getSecretValues()))
                        .build();

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
            if (shouldPrintDebugLog(credentials, user)
                    && context.getRequest().getProvider().isOpenBanking()) {
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
                                getFormattedFileName(logContent, credentials));
            }

            if (!agentDebugStorage.isLocalStorage()) {
                logFile = new File(getFormattedFileName(logContent, credentials));
            }

            if (Objects.isNull(logFile)) {
                log.warn(
                        "Created debug log but local storage cannot be used & no S3 storage configuration available.");
                return;
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
                log.info("Flushed debug log for further investigation: " + storagePath);
            }

        } catch (EmptyDebugLogException e) {
            log.info(e.getMessage());
        } catch (IOException e) {
            log.error("Could not write debug logFile.", e);
        }
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

    private String getFormattedFileName(String logContent, Credentials credentials) {
        return String.format(
                        "%s_%s_u%s_c%s_%s.log",
                        credentials.getProviderName(),
                        ThreadSafeDateFormat.FORMATTER_FILENAME_SAFE.format(new Date()),
                        credentials.getUserId(),
                        credentials.getId(),
                        getFormattedSize(logContent))
                .replace(":", ".");
    }

    private boolean shouldPrintDebugLogRegardless() {
        return state.getDebugFrequencyPercent() > RandomUtils.randomInt(100);
    }
}
