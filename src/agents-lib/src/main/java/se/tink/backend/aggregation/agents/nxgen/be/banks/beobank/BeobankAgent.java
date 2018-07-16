package se.tink.backend.aggregation.agents.nxgen.be.banks.beobank;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationAgent;
import se.tink.backend.aggregation.rpc.CredentialsRequest;

public class BeobankAgent extends EuroInformationAgent {
    public BeobankAgent(CredentialsRequest request,
            AgentContext context) {
        super(request, context, new BeobankConfiguration());
    }
}
