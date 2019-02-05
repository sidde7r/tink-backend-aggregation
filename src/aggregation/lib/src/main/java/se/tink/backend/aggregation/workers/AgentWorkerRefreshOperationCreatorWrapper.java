package se.tink.backend.aggregation.workers;

import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.cluster.identification.ClientInfo;
import se.tink.libraries.credentials.service.RefreshInformationRequest;

public class AgentWorkerRefreshOperationCreatorWrapper implements Runnable {

    private AgentWorkerOperationFactory agentWorkerCommandFactory;
    private RefreshInformationRequest request;
    private ClientInfo clientInfo;

    AgentWorkerRefreshOperationCreatorWrapper(AgentWorkerOperationFactory agentWorkerCommandFactory, RefreshInformationRequest request, ClientInfo clientInfo) {
        this.agentWorkerCommandFactory = agentWorkerCommandFactory;
        this.request = request;
        this.clientInfo = clientInfo;
    }

    @Override
    public void run() {
        AgentWorkerOperation agentWorkerOperation = agentWorkerCommandFactory
                .createOperationRefresh(request, clientInfo);
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
            ClientInfo clientInfo) {
        return new AgentWorkerRefreshOperationCreatorWrapper(agentWorkerOperationFactory, refreshInformationRequest, clientInfo);
    }
}
