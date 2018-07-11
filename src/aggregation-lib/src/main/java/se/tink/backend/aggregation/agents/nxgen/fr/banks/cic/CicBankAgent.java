package se.tink.backend.aggregation.agents.nxgen.fr.banks.cic;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationAgent;
import se.tink.backend.aggregation.rpc.CredentialsRequest;

public class CicBankAgent extends EuroInformationAgent {
    public CicBankAgent(CredentialsRequest request,
            AgentContext context) {
        super(request, context, new CicBankconfiguration());
    }
}
