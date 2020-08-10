package se.tink.backend.aggregation.agents.nxgen.it.openbanking.cartalis;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.FabricAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

public class CartalisAgent extends FabricAgent {
    private static final String BASE_URL = "https://psdgw-cartalis.fabrick.com";

    @Inject
    public CartalisAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
    }

    @Override
    public String getBaseUrl() {
        return BASE_URL;
    }
}
