package se.tink.backend.aggregation.agents.nxgen.fr.banks.monabankq;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationAgent;
import se.tink.backend.aggregation.rpc.CredentialsRequest;

public class MonaBankqAgent extends EuroInformationAgent {
    public MonaBankqAgent(CredentialsRequest request,
            AgentContext context) {
        super(request, context, new MonaBanqConfiguration());
    }
}
