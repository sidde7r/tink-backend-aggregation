package se.tink.backend.aggregation.agents.nxgen.fr.banks.creditmutuel;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationAgent;
import se.tink.backend.aggregation.rpc.CredentialsRequest;

public class CreditMutuelAgent extends EuroInformationAgent {

    public CreditMutuelAgent(CredentialsRequest request, AgentContext context, String signatureKeyPath) {
        super(request, context, signatureKeyPath, new CreditMutuelConfiguration());
    }
}
