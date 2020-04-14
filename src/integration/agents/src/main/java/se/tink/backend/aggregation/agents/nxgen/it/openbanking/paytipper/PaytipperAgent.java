package se.tink.backend.aggregation.agents.nxgen.it.openbanking.paytipper;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.FabricAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

public class PaytipperAgent extends FabricAgent {
    @Inject
    public PaytipperAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
    }
}
