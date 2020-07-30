package se.tink.backend.aggregation.agents.nxgen.it.openbanking.bancacarige;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeApiClient;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

public class BancaCarigeAgent extends CbiGlobeAgent {
    @Inject
    public BancaCarigeAgent(AgentComponentProvider agentComponentProvider) {
        super(agentComponentProvider);
    }

    @Override
    protected CbiGlobeApiClient getApiClient(boolean requestManual) {
        return new BancaCarigeApiClient(
                client,
                persistentStorage,
                sessionStorage,
                requestManual,
                temporaryStorage,
                getProviderConfiguration());
    }
}
