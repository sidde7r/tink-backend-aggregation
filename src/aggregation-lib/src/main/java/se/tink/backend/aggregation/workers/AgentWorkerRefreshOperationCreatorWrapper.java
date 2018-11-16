package se.tink.backend.aggregation.workers;

import se.tink.backend.aggregation.cluster.identification.ClientInfo;
import se.tink.backend.aggregation.cluster.identification.ClusterInfo;
import se.tink.backend.aggregation.rpc.Provider;
import se.tink.backend.aggregation.rpc.RefreshInformationRequest;

public class AgentWorkerRefreshOperationCreatorWrapper implements Runnable {

    private AgentWorkerOperationFactory agentWorkerCommandFactory;
    private RefreshInformationRequest request;
    private ClusterInfo clusterInfo;
    private ClientInfo clientInfo;

    AgentWorkerRefreshOperationCreatorWrapper(AgentWorkerOperationFactory agentWorkerCommandFactory, RefreshInformationRequest request, ClusterInfo clusterInfo, ClientInfo clientInfo) {
        this.agentWorkerCommandFactory = agentWorkerCommandFactory;
        this.request = request;
        this.clusterInfo = clusterInfo;
        this.clientInfo = clientInfo;
    }

    @Override
    public void run() {
        AgentWorkerOperation agentWorkerOperation = agentWorkerCommandFactory
                .createRefreshOperation(clusterInfo, request, clientInfo);
        agentWorkerOperation.run();
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

    public ClientInfo getClientInfo() {
        return clientInfo;
    }

    public static AgentWorkerRefreshOperationCreatorWrapper of(AgentWorkerOperationFactory agentWorkerOperationFactory,
            RefreshInformationRequest refreshInformationRequest,
            ClusterInfo clusterInfo, ClientInfo clientInfo) {
        return new AgentWorkerRefreshOperationCreatorWrapper(agentWorkerOperationFactory, refreshInformationRequest, clusterInfo, clientInfo);
    }
}
