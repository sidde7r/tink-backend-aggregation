package se.tink.backend.aggregation.agents.nxgen.fi.banks.saastopankki;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.SamlinkAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.SamlinkConfiguration;
import se.tink.backend.aggregation.rpc.CredentialsRequest;

public class SpAgent extends SamlinkAgent {

    public SpAgent(CredentialsRequest request, AgentContext context) {
        super(request, context, new SamlinkConfiguration(SpConstants.Url.BASE));
    }
}
