package se.tink.backend.aggregation.workers.commands;

import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.agents.agent.AgentVisitor;
import se.tink.backend.aggregation.nxgen.agents.SubsequentGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AuthenticationControllerType;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class ManualOrAutoAuthenticationAgentVisitor implements AgentVisitor {

    private boolean manualAuthentication;
    private CredentialsRequest request;

    public ManualOrAutoAuthenticationAgentVisitor(CredentialsRequest request) {
        this.request = request;
        manualAuthentication = false;
    }

    @Override
    public void visit(Agent agent) {
        if (agent instanceof SubsequentGenerationAgent) {
            SubsequentGenerationAgent<?> subsequentGenerationAgent =
                    (SubsequentGenerationAgent<?>) agent;
            if (subsequentGenerationAgent.getAuthenticator()
                    instanceof AuthenticationControllerType) {
                manualAuthentication =
                        ((AuthenticationControllerType)
                                        subsequentGenerationAgent.getAuthenticator())
                                .isManualAuthentication(request);
            }
        }
        // Legacy Agents
        else {
            if (request.getProvider().getCredentialsType() != CredentialsTypes.PASSWORD) {
                manualAuthentication = true;
            }
        }
    }

    boolean isManualAuthentication() {
        return manualAuthentication;
    }
}
