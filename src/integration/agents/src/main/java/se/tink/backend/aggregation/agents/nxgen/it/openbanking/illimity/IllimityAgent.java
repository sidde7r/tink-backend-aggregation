package se.tink.backend.aggregation.agents.nxgen.it.openbanking.illimity;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.FabricAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

public class IllimityAgent extends FabricAgent {

    private static final String BASE_URL = "https://psdgw-illimity.fabrick.com";

    @Inject
    public IllimityAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
    }

    @Override
    public String getBaseUrl() {
        return BASE_URL;
    }
}
