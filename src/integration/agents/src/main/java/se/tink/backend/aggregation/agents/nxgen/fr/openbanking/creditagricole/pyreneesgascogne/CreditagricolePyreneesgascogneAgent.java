package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.pyreneesgascogne;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleBaseAgent;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class CreditagricolePyreneesgascogneAgent extends CreditAgricoleBaseAgent {
    public CreditagricolePyreneesgascogneAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
    }
}
