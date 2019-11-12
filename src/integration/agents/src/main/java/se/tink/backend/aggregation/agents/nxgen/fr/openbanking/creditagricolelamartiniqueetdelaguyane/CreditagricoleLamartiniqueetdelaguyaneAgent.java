package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricolelamartiniqueetdelaguyane;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.BankEnum;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleBaseAgent;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class CreditagricoleLamartiniqueetdelaguyaneAgent extends CreditAgricoleBaseAgent {
    public CreditagricoleLamartiniqueetdelaguyaneAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair, BankEnum.LA_MARTINIQUE_ET_DE_LA_GUYANE);
    }
}
