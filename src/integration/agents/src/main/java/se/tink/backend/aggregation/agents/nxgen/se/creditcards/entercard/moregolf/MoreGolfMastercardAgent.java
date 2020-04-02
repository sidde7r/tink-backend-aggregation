package se.tink.backend.aggregation.agents.nxgen.se.creditcards.entercard.moregolf;

import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.entercard.EnterCardAgent;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class MoreGolfMastercardAgent extends EnterCardAgent {

    public MoreGolfMastercardAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair, new MoreGolfMastercardConfiguration());
    }
}
