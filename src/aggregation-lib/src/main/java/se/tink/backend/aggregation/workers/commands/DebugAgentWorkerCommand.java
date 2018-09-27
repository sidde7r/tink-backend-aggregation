package se.tink.backend.aggregation.workers.commands;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Objects;
import java.util.List;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.rpc.CredentialsRequestType;
import se.tink.backend.aggregation.rpc.CredentialsStatus;
import se.tink.backend.aggregation.rpc.Field;
import se.tink.backend.aggregation.rpc.TransferRequest;
import se.tink.backend.aggregation.rpc.User;
import se.tink.backend.aggregation.storage.AgentDebugStorageHandler;
import se.tink.backend.aggregation.workers.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.AgentWorkerCommandResult;
import se.tink.backend.aggregation.workers.AgentWorkerContext;
import se.tink.backend.aggregation.workers.commands.state.DebugAgentWorkerCommandState;
import se.tink.libraries.uuid.UUIDUtils;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.core.transfer.SignableOperationStatuses;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class DebugAgentWorkerCommand extends AgentWorkerCommand {

    private static final AggregationLogger log = new AggregationLogger(DebugAgentWorkerCommand.class);

    private DebugAgentWorkerCommandState state;
    private AgentWorkerContext context;
    private AgentDebugStorageHandler agentDebugStorage;

    public DebugAgentWorkerCommand(AgentWorkerContext context,
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
        String clusterId = context.getClusterInfo().getClusterId().getId();
        List<String> excludedDebugClusters = context.getServiceContext().getConfiguration()
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
                logContent = logContent.replace(credentialFieldValue, "***" + providerField + "***");
            }
        }

        return logContent;
    }

    private void writeToDebugFile(Credentials credentials, TransferRequest transferRequest) {
        try {
            File debugDirectory = state.getDebugDirectory();
            String logContent = context.getLogOutputStream().toString("UTF-8");
            logContent = maskSensitiveOutputLog(logContent, credentials);

            File logFile = new File(debugDirectory, String.format(
                    "%s_%s_u%s_c%s.log",
                    credentials.getProviderName(),
                    ThreadSafeDateFormat.FORMATTER_FILENAME_SAFE.format(new Date()),
                    credentials.getUserId(),
                    credentials.getId()));

            String storagePath = agentDebugStorage.store(logContent, logFile);

            if (transferRequest != null) {
                String id = UUIDUtils.toTinkUUID(transferRequest.getTransfer().getId());
                log.info("Flushed transfer (" + id + ") debug log for further investigation: " + storagePath);
            } else {
                log.info("Flushed debug log for further investigation: " + storagePath);
            }

        } catch (IOException e) {
            log.error("Could not write logfile for: (provider:"
                    + credentials.getProviderName() + ", credentialsId:" + credentials.getId() + ", status:"
                    + credentials.getStatus() + ", userId:" + credentials.getUserId() + ")", e);
        }
    }
}
