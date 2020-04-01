package se.tink.backend.aggregation.agents.nxgen.se.creditcards.sebkort.ingo;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.SebKortAgent;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class IngoMastercardAgent extends SebKortAgent {
    public IngoMastercardAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair, new IngoMastercardConfiguration());
    }
}
