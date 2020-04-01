package se.tink.backend.aggregation.agents.nxgen.se.creditcards.sebkortv2.nknyckeln;

import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkortv2.SebKortAgent;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class NkNyckelnMastercardAgent extends SebKortAgent {
    public NkNyckelnMastercardAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair, new NkNyckelnMastercardConfiguration());
    }
}
