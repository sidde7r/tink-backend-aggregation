package se.tink.backend.aggregation.agents.nxgen.se.creditcards.sebkort.nordicchoice;

import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.SebKortAgent;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class NordicChoiceClubMastercardSEAgent extends SebKortAgent {
    public NordicChoiceClubMastercardSEAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair, new NordicChoiceClubMastercardSEConfiguration());
    }
}
