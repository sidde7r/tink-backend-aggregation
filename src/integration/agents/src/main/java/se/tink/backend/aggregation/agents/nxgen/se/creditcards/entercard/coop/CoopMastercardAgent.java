package se.tink.backend.aggregation.agents.nxgen.se.creditcards.entercard.coop;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.entercard.EnterCardAgent;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class CoopMastercardAgent extends EnterCardAgent {

    public CoopMastercardAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair, new CoopMastercardConfiguration());
    }
}
