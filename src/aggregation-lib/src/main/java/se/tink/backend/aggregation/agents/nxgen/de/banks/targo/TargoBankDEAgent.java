package se.tink.backend.aggregation.agents.nxgen.de.banks.targo;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank.TargoBankAgent;
import se.tink.backend.aggregation.rpc.CredentialsRequest;

public class TargoBankDEAgent extends TargoBankAgent {
    public TargoBankDEAgent(CredentialsRequest request,
            AgentContext context) {
        super(request, context, new TargoBankDEConfiguration());
    }
}
