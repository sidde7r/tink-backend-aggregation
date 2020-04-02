package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.chamoagnebourgogne;

import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleBaseAgent;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class CreditagricoleChamoagnebourgogneAgent extends CreditAgricoleBaseAgent {
    public CreditagricoleChamoagnebourgogneAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
    }
}
