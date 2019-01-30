package se.tink.backend.aggregation.agents.nxgen.se.creditcards.sebkort.finnair;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.SebKortAgent;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.libraries.credentials_requests.CredentialsRequest;

public class FinnairMastercardSEAgent extends SebKortAgent {
    public FinnairMastercardSEAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair, new FinnairMastercardSEConfiguration());
    }
}
