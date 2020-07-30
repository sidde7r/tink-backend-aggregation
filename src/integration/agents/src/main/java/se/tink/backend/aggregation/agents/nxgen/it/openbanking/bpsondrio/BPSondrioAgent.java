package se.tink.backend.aggregation.agents.nxgen.it.openbanking.bpsondrio;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeApiClient;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

public class BPSondrioAgent extends CbiGlobeAgent {

    @Inject
    public BPSondrioAgent(AgentComponentProvider agentComponentProvider) {
        super(agentComponentProvider);
    }

    @Override
    protected CbiGlobeApiClient getApiClient(boolean requestManual) {
        return new BPSondrioApiClient(
                client,
                persistentStorage,
                sessionStorage,
                requestManual,
                temporaryStorage,
                getProviderConfiguration());
    }
}
