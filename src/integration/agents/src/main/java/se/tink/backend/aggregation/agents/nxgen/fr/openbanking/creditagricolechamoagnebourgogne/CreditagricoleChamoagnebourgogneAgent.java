package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricolechamoagnebourgogne;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.BankEnum;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleBaseAgent;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class CreditagricoleChamoagnebourgogneAgent extends CreditAgricoleBaseAgent {

    public CreditagricoleChamoagnebourgogneAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair, BankEnum.CHAMPAGNE_BOURGOGNE);
    }
}
