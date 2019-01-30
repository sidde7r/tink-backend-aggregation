package se.tink.backend.aggregation.agents.nxgen.se.creditcards.sebkort.djurgardskortet;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.SebKortAgent;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class DjurgardskortetMastercardAgent extends SebKortAgent {
    public DjurgardskortetMastercardAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair, new DjurgardskortetMastercardConfiguration());
    }
}
