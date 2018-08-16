package se.tink.backend.aggregation.workers;

import se.tink.backend.aggregation.cluster.identification.ClusterInfo;
import se.tink.backend.aggregation.rpc.Provider;
import se.tink.backend.aggregation.rpc.RefreshInformationRequest;
import se.tink.backend.queue.AutomaticRefreshStatus;
import se.tink.backend.queue.QueuableJob;

public class AgentWorkerRefreshOperationCreatorWrapper implements Runnable, QueuableJob {

    private AgentWorkerOperationFactory agentWorkerCommandFactory;
    private RefreshInformationRequest request;
    private ClusterInfo clusterInfo;
    private AutomaticRefreshStatus status;


    AgentWorkerRefreshOperationCreatorWrapper(AgentWorkerOperationFactory agentWorkerCommandFactory, RefreshInformationRequest request, ClusterInfo clusterInfo) {
        this.agentWorkerCommandFactory = agentWorkerCommandFactory;
        this.request = request;
        this.clusterInfo = clusterInfo;
        this.status = AutomaticRefreshStatus.NOT_INITIALIZED;
    }

    @Override
    public void run() {
        AgentWorkerOperation agentWorkerOperation = agentWorkerCommandFactory
                .createRefreshOperation(clusterInfo, request);
        try {
            this.status = AutomaticRefreshStatus.RUNNING;
            agentWorkerOperation.run();
            this.status = AutomaticRefreshStatus.SUCCESS;
        } catch (Exception e) {
            this.status = AutomaticRefreshStatus.FAILED;
            this.status.setError(e.getMessage());
        }
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

    @Override
    public AutomaticRefreshStatus getStatus() {
        return this.status;
    }
}
