package se.tink.backend.aggregation.agents.banks.nordea;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;

public class NordeaAgent extends NordeaV20Agent {
    public NordeaAgent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
    }
}
