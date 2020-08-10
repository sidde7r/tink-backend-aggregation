package se.tink.backend.aggregation.agents.nxgen.it.openbanking.tim;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.FabricAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

public class TimAgent extends FabricAgent {
    private static final String BASE_URL = "https://psdgw-tim.fabrick.com";

    @Inject
    public TimAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
    }

    @Override
    public String getBaseUrl() {
        return BASE_URL;
    }
}
