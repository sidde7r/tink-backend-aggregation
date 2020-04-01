package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.alsacevosges;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleBaseAgent;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class CreditAgricoleAlsacevosgesAgent extends CreditAgricoleBaseAgent {
    public CreditAgricoleAlsacevosgesAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
    }
}
