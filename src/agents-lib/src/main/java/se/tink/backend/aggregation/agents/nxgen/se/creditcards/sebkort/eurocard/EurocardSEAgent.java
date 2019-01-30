package se.tink.backend.aggregation.agents.nxgen.se.creditcards.sebkort.eurocard;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.SebKortAgent;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class EurocardSEAgent extends SebKortAgent {
    public EurocardSEAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair, new EurocardSEConfiguration());
    }
}
