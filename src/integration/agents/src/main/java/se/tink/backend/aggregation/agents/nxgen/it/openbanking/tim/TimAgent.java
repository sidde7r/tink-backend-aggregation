package se.tink.backend.aggregation.agents.nxgen.it.openbanking.tim;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.FabricAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

public class TimAgent extends FabricAgent {
    @Inject
    public TimAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
    }
}
