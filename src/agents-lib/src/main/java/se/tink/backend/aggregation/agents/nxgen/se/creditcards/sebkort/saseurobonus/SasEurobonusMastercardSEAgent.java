package se.tink.backend.aggregation.agents.nxgen.se.creditcards.sebkort.saseurobonus;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.SebKortAgent;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class SasEurobonusMastercardSEAgent extends SebKortAgent {
    public SasEurobonusMastercardSEAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair, new SasEurobonusMastercardSEConfiguration());
    }
}
