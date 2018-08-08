package se.tink.backend.aggregation.agents.nxgen.fi.banks.poppankki;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.SamlinkAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.SamlinkConfiguration;
import se.tink.backend.aggregation.rpc.CredentialsRequest;

public class PopPankkiAgent extends SamlinkAgent {

    public PopPankkiAgent(CredentialsRequest request, AgentContext context, String signatureKeyPath) {
        super(request, context, signatureKeyPath, new SamlinkConfiguration(PopPankkiConstants.Url.BASE));
    }
}
