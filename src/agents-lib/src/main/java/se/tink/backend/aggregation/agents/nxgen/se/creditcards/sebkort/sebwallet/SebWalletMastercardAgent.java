package se.tink.backend.aggregation.agents.nxgen.se.creditcards.sebkort.sebwallet;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkort.SebKortAgent;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.rpc.CredentialsRequest;

public class SebWalletMastercardAgent extends SebKortAgent {
    public SebWalletMastercardAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair, new SebWalletMastercardConfiguration());
    }
}
