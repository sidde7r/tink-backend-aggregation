package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.banquechalus;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.BankEnum;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleBaseAgent;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class CreditAgricoleBanquechalusAgent extends CreditAgricoleBaseAgent {
    public CreditAgricoleBanquechalusAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair, BankEnum.ALPES_PROVENCE);
    }
}
