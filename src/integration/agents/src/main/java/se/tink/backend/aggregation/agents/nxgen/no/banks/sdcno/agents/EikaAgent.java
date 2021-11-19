package se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.agents;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.SdcNoAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentCapabilities
public final class EikaAgent extends SdcNoAgent {
    @Inject
    public EikaAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
    }
}
