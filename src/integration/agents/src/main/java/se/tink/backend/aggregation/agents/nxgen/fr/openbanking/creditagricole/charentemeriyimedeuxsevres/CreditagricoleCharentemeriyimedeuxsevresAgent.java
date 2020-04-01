package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.charentemeriyimedeuxsevres;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleBaseAgent;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class CreditagricoleCharentemeriyimedeuxsevresAgent extends CreditAgricoleBaseAgent {
    public CreditagricoleCharentemeriyimedeuxsevresAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
    }
}
