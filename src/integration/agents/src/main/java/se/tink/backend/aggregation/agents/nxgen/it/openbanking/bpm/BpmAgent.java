package se.tink.backend.aggregation.agents.nxgen.it.openbanking.bpm;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeApiClient;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

public class BpmAgent extends CbiGlobeAgent {

    @Inject
    public BpmAgent(AgentComponentProvider agentComponentProvider) {
        super(agentComponentProvider);
    }

    @Override
    protected CbiGlobeApiClient getApiClient(boolean requestManual) {
        return new BpmApiClient(
                client,
                persistentStorage,
                sessionStorage,
                requestManual,
                temporaryStorage,
                getProviderConfiguration());
    }
}
