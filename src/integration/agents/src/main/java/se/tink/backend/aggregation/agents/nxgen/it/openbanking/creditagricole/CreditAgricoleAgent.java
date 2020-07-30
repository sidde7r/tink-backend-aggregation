package se.tink.backend.aggregation.agents.nxgen.it.openbanking.creditagricole;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

public class CreditAgricoleAgent extends CbiGlobeAgent {

    @Inject
    public CreditAgricoleAgent(AgentComponentProvider agentComponentProvider) {
        super(agentComponentProvider);
    }
}
