package se.tink.backend.aggregation.agents.nxgen.fr.banks.bmcedirect;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationAgent;
import se.tink.backend.aggregation.rpc.CredentialsRequest;

public class BmceDirectAgent extends EuroInformationAgent {
    public BmceDirectAgent(CredentialsRequest request,
            AgentContext context) {
        super(request, context, new BmceDirectConfiguration());
    }
}
