package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.latourauneetdupoitou;

import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleBaseAgent;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class CreditagricoleLatourauneetdupoitouAgent extends CreditAgricoleBaseAgent {
    public CreditagricoleLatourauneetdupoitouAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
    }
}
