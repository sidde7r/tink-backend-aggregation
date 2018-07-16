package se.tink.backend.aggregation.agents.nxgen.fr.banks.monabanq;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationAgent;
import se.tink.backend.aggregation.rpc.CredentialsRequest;

public class MonaBanqAgent extends EuroInformationAgent {
    public MonaBanqAgent(CredentialsRequest request,
            AgentContext context) {
        super(request, context, new MonaBanqConfiguration());
    }
}
