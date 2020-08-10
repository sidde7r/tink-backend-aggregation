package se.tink.backend.aggregation.agents.nxgen.it.openbanking.paytipper;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.FabricAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

public class PaytipperAgent extends FabricAgent {
    private static final String BASE_URL = "https://psdgw-paytipper.fabrick.com";

    @Inject
    public PaytipperAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
    }

    @Override
    public String getBaseUrl() {
        return BASE_URL;
    }
}
