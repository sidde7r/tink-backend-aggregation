package se.tink.backend.aggregation.workers.commands;

import java.util.Objects;
import se.tink.backend.aggregation.aggregationcontroller.AggregationControllerAggregationClient;
import se.tink.backend.aggregation.aggregationcontroller.v1.core.HostConfiguration;
import se.tink.backend.aggregation.cluster.identification.ClusterInfo;
import se.tink.backend.aggregation.converter.HostConfigurationConverter;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsStatus;
import se.tink.backend.aggregation.rpc.Provider;
import se.tink.backend.aggregation.rpc.ProviderStatuses;
import se.tink.backend.aggregation.workers.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.AgentWorkerCommandResult;
import se.tink.backend.aggregation.workers.AgentWorkerContext;
import se.tink.backend.common.mapper.CoreCredentialsMapper;

public class ValidateProviderAgentWorkerStatus extends AgentWorkerCommand {
    private final AgentWorkerContext context;
    private final AggregationControllerAggregationClient aggregationControllerAggregationClient;
    private final HostConfiguration hostConfiguration;

    public ValidateProviderAgentWorkerStatus(AgentWorkerContext context,
                                             AggregationControllerAggregationClient aggregationControllerAggregationClient) {
        this.context = context;
        this.aggregationControllerAggregationClient = aggregationControllerAggregationClient;
        this.hostConfiguration = context.getHostConfiguration();
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

        se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateCredentialsStatusRequest updateCredentialsStatusRequest =
                new se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateCredentialsStatusRequest();

        updateCredentialsStatusRequest.setCredentials(coreCredentials);
        updateCredentialsStatusRequest.setUserId(credentials.getUserId());

        aggregationControllerAggregationClient.updateCredentials(hostConfiguration, updateCredentialsStatusRequest);

        return AgentWorkerCommandResult.ABORT;
    }

    @Override
    public void postProcess() throws Exception {
        // Deliberately left empty.
    }
}
