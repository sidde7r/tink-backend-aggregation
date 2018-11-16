package se.tink.backend.aggregation.workers.commands;

import com.google.common.base.Strings;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Objects;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.rpc.CredentialsRequestType;
import se.tink.backend.aggregation.rpc.CredentialsStatus;
import se.tink.backend.aggregation.rpc.Field;
import se.tink.backend.aggregation.rpc.TransferRequest;
import se.tink.backend.aggregation.rpc.User;
import se.tink.backend.aggregation.storage.debug.AgentDebugLocalStorage;
import se.tink.backend.aggregation.storage.debug.AgentDebugS3Storage;
import se.tink.backend.aggregation.storage.debug.AgentDebugStorageHandler;
import se.tink.backend.aggregation.workers.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.AgentWorkerCommandResult;
import se.tink.backend.aggregation.workers.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.commands.exception.EmptyDebugLogException;
import se.tink.backend.aggregation.workers.commands.state.DebugAgentWorkerCommandState;
import se.tink.libraries.uuid.UUIDUtils;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.core.transfer.SignableOperationStatuses;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class DebugAgentWorkerCommand extends AgentWorkerCommand {

    private static final Logger log = LoggerFactory.getLogger(DebugAgentWorkerCommand.class);

    private DebugAgentWorkerCommandState state;
    private AgentWorkerCommandContext context;
    private AgentDebugStorageHandler agentDebugStorage;

    public DebugAgentWorkerCommand(AgentWorkerCommandContext context,
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
        String clusterId = context.getCallbackHostConfiguration().getClusterId();
        List<String> excludedDebugClusters = context.getAgentsServiceConfiguration()
                .getExcludedDebugClusters().getExcludedClusters();

        if (Objects.nonNull(excludedDebugClusters) && excludedDebugClusters.contains(clusterId)) {
            return;
        }

        Credentials credentials = context.getRequest().getCredentials();
        
        if (context.getRequest().getType() == CredentialsRequestType.TRANSFER) {
            TransferRequest transferRequest = (TransferRequest) context.getRequest();

            // Debug output for transfers.
            SignableOperationStatuses transferStatus = transferRequest.getSignableOperation().getStatus();
            if (transferStatus == SignableOperationStatuses.FAILED
                    || transferStatus == SignableOperationStatuses.CANCELLED) {
                writeToDebugFile(credentials, transferRequest);
            }
        } else {
            User user = context.getRequest().getUser();

            // Debug output for non-transfers such as refresh commands and delete.
            if (credentials.getStatus() == CredentialsStatus.AUTHENTICATION_ERROR ||
                    credentials.getStatus() == CredentialsStatus.TEMPORARY_ERROR ||
                    credentials.getStatus() == CredentialsStatus.UNCHANGED ||
                    credentials.isDebug() ||
                    user.isDebug()) {
                writeToDebugFile(credentials, null);
            }
        }
    }

    private String maskSensitiveOutputLog(String logContent, Credentials credentials){
        for (Field providerField : context.getRequest().getProvider().getFields()) {
            String credentialFieldValue = credentials.getField(providerField.getName());


            if (Objects.nonNull(credentialFieldValue)) {
                logContent = logContent.replace(credentialFieldValue, "***" + providerField.getName() + "***");
            }
        }

        return logContent;
    }

    private static String getFormattedSize(String str) {
        int lines = str.split("\n").length;
        int bytesUtf8 = str.getBytes(StandardCharsets.UTF_8).length;
        return String.format("%dB_%d", bytesUtf8, lines);
    }

    private void writeToDebugFile(Credentials credentials, TransferRequest transferRequest) {
        try {
            String storagePath = null;

            if (agentDebugStorage instanceof AgentDebugLocalStorage && state.isSaveLocally()) {
                storagePath = handleLocalStorage(credentials);
            }

            if (agentDebugStorage instanceof AgentDebugS3Storage) {
                storagePath = handleS3Storage(credentials);
            }

            if (Strings.isNullOrEmpty(storagePath)) {
                return;
            }

            if (transferRequest != null) {
                String id = UUIDUtils.toTinkUUID(transferRequest.getTransfer().getId());
                log.info("Flushed transfer (" + id + ") debug log for further investigation: " + storagePath);
            } else {
                log.info("Flushed debug log for further investigation: " + storagePath);
            }

        } catch (EmptyDebugLogException e) {
            log.info(e.getMessage());
        } catch (IOException e) {
            log.error("Could not write debug logFile.", e);
        }
    }

    private String handleLocalStorage(Credentials credentials) throws IOException, EmptyDebugLogException {
        File debugDirectory = state.getDebugDirectory();
        String logContent = getCleanLogContent(credentials);

        File logFile = new File(debugDirectory, getFormattedFileName(logContent, credentials));

        return agentDebugStorage.store(logContent, logFile);
    }

    private String handleS3Storage(Credentials credentials) throws IOException, EmptyDebugLogException {
        String logContent = getCleanLogContent(credentials);
        File logFile = new File(getFormattedFileName(logContent, credentials));

        return agentDebugStorage.store(logContent, logFile);
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
}
