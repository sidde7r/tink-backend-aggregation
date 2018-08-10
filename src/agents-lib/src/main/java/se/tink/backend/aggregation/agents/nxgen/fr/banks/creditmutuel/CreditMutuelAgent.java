package se.tink.backend.aggregation.agents.nxgen.fr.banks.creditmutuel;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationAgent;
import se.tink.backend.aggregation.rpc.CredentialsRequest;
import se.tink.backend.common.config.SignatureKeyPair;

public class CreditMutuelAgent extends EuroInformationAgent {

    public CreditMutuelAgent(CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair, new CreditMutuelConfiguration());
    }
}
