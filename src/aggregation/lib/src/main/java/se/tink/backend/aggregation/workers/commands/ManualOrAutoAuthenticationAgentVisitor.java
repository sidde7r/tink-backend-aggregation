package se.tink.backend.aggregation.workers.commands;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.Agent;
import se.tink.backend.aggregation.agents.AgentVisitor;
import se.tink.backend.aggregation.nxgen.agents.SubsequentGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AuthenticationControllerType;

public class ManualOrAutoAuthenticationAgentVisitor implements AgentVisitor {

    private boolean manualAuthentication;
    private Credentials credentials;

    public ManualOrAutoAuthenticationAgentVisitor(Credentials credentials) {
        this.credentials = credentials;
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
                                .isManualAuthentication(credentials);
            }
        }
    }

    boolean isManualAuthentication() {
        return manualAuthentication;
    }
}
