package se.tink.backend.aggregation.agents.banks.nordea;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.rpc.CredentialsRequest;

public class NordeaAgent extends NordeaV20Agent {
    public NordeaAgent(CredentialsRequest request, AgentContext context) {
        super(request, context);
    }
}
