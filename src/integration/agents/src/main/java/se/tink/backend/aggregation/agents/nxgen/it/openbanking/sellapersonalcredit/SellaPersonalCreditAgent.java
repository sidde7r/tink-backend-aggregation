package se.tink.backend.aggregation.agents.nxgen.it.openbanking.sellapersonalcredit;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.FabricAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

public class SellaPersonalCreditAgent extends FabricAgent {
    private static final String BASE_URL = "https://psdgw-sellapersonalcredit.fabrick.com";

    @Inject
    public SellaPersonalCreditAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
    }

    @Override
    public String getBaseUrl() {
        return BASE_URL;
    }
}
