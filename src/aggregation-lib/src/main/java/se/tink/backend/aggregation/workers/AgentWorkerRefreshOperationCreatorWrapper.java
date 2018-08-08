package se.tink.backend.aggregation.workers;

import se.tink.backend.aggregation.cluster.identification.ClusterInfo;
import se.tink.backend.aggregation.rpc.Provider;
import se.tink.backend.aggregation.rpc.RefreshInformationRequest;

public class AgentWorkerRefreshOperationCreatorWrapper implements Runnable {

    private AgentWorkerOperationFactory agentWorkerCommandFactory;
    private RefreshInformationRequest request;
    private ClusterInfo clusterInfo;

    AgentWorkerRefreshOperationCreatorWrapper(AgentWorkerOperationFactory agentWorkerCommandFactory, RefreshInformationRequest request, ClusterInfo clusterInfo) {
        this.agentWorkerCommandFactory = agentWorkerCommandFactory;
        this.request = request;
        this.clusterInfo = clusterInfo;
    }

    @Override
    public void run() {
        AgentWorkerOperation agentWorkerOperation = agentWorkerCommandFactory.createRefreshOperation(clusterInfo, request);
        agentWorkerOperation.run();
    }

    public static AgentWorkerRefreshOperationCreatorWrapper of(AgentWorkerOperationFactory agentWorkerOperationFactory, RefreshInformationRequest refreshInformationRequest, ClusterInfo clusterInfo) {
        return new AgentWorkerRefreshOperationCreatorWrapper(agentWorkerOperationFactory, refreshInformationRequest, clusterInfo);
    }

    public Provider getProvider() {
        return request.getProvider();
    }

    public String getCredentialsId() {
        return request.getCredentials().getId();
    }

    public String getProviderName() {
        return request.getProvider().getName();
    }
}
