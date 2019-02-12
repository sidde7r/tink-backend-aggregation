package se.tink.backend.aggregation.agents.nxgen.se.creditcards.sebkortv2.sjprio;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkortv2.SebKortAgent;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class SjPrioMastercardAgent extends SebKortAgent {
    public SjPrioMastercardAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair, new SjPrioMastercardConfiguration());
    }
}
