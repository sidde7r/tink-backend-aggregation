package se.tink.backend.aggregation.agents.nxgen.it.openbanking.bancapatrimoni;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.FabricAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

public class BancaPatrimoniAgent extends FabricAgent {
    @Inject
    public BancaPatrimoniAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
    }
}
