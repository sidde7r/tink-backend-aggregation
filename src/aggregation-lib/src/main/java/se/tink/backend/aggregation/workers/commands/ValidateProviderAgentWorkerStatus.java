package se.tink.backend.aggregation.workers.commands;

import java.util.Objects;
import se.tink.backend.aggregation.aggregationcontroller.AggregationControllerAggregationClient;
import se.tink.backend.aggregation.cluster.identification.ClusterInfo;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsStatus;
import se.tink.backend.aggregation.rpc.Provider;
import se.tink.backend.aggregation.rpc.ProviderStatuses;
import se.tink.backend.aggregation.workers.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.AgentWorkerCommandResult;
import se.tink.backend.aggregation.workers.AgentWorkerContext;
import se.tink.backend.common.mapper.CoreCredentialsMapper;
import se.tink.backend.system.rpc.UpdateCredentialsStatusRequest;

public class ValidateProviderAgentWorkerStatus extends AgentWorkerCommand {
    private AgentWorkerContext context;
    private boolean useAggregationController;
    private AggregationControllerAggregationClient aggregationControllerAggregationClient;
    private ClusterInfo clusterInfo;
    private boolean isAggregationCluster;

    public ValidateProviderAgentWorkerStatus(AgentWorkerContext context,
            boolean useAggregationController,
            AggregationControllerAggregationClient aggregationControllerAggregationClient,
            boolean isAggregationCluster, ClusterInfo clusterInfo) {
        this.context = context;
        this.useAggregationController = useAggregationController;
        this.aggregationControllerAggregationClient = aggregationControllerAggregationClient;
        this.isAggregationCluster = isAggregationCluster;
        this.clusterInfo = clusterInfo;
    }

    @Override
    public AgentWorkerCommandResult execute() throws Exception {
        Provider provider = context.getRequest().getProvider();
        Credentials credentials = context.getRequest().getCredentials();

        if (!Objects.equals(provider.getStatus(), ProviderStatuses.TEMPORARY_DISABLED) &&
                !Objects.equals(provider.getStatus(), ProviderStatuses.DISABLED)) {
            return AgentWorkerCommandResult.CONTINUE;
        }

        credentials.setStatus(CredentialsStatus.UNCHANGED);
        credentials.clearSensitiveInformation(provider);

        // TODO: Refactor System API side to not depend on :main-api
        se.tink.backend.core.Credentials coreCredentials = CoreCredentialsMapper
                .fromAggregationCredentials(credentials);

        if (useAggregationController) {
            se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateCredentialsStatusRequest updateCredentialsStatusRequest =
                    new se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateCredentialsStatusRequest();

            updateCredentialsStatusRequest.setCredentials(coreCredentials);
            updateCredentialsStatusRequest.setUserId(credentials.getUserId());

            if (isAggregationCluster) {
                aggregationControllerAggregationClient.updateCredentials(clusterInfo, updateCredentialsStatusRequest);
            } else {
                aggregationControllerAggregationClient.updateCredentials(updateCredentialsStatusRequest);
            }
        } else {
            UpdateCredentialsStatusRequest updateCredentialsRequest = new UpdateCredentialsStatusRequest();

            updateCredentialsRequest.setCredentials(coreCredentials);
            updateCredentialsRequest.setUserId(credentials.getUserId());

            context.getSystemServiceFactory().getUpdateService().updateCredentials(updateCredentialsRequest);
        }

        return AgentWorkerCommandResult.ABORT;
    }

    @Override
    public void postProcess() throws Exception {
        // Deliberately left empty.
    }
}
