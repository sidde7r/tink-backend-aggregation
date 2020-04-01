package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.sparekassensjaellandfyn;

import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.BankdataAgent;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class SparekassenSjaellandFynAgent extends BankdataAgent {

    public SparekassenSjaellandFynAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(
                request,
                context,
                signatureKeyPair,
                SparekassenSjaellandFynConstants.BASE_URL,
                SparekassenSjaellandFynConstants.BASE_AUTH_URL);
    }
}
