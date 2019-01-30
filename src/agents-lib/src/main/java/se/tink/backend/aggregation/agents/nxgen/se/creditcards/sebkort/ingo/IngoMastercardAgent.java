package se.tink.backend.aggregation.agents.nxgen.se.creditcards.sebkort.ingo;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.SebKortAgent;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.libraries.credentials_requests.CredentialsRequest;

public class IngoMastercardAgent extends SebKortAgent {
    public IngoMastercardAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair, new IngoMastercardConfiguration());
    }
}
