package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bnpparibas;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.BnpParibasBaseAgent;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class BnpParibasAgent extends BnpParibasBaseAgent {
    public BnpParibasAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
    }
}
