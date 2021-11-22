package se.tink.backend.aggregation.agents.nxgen.hu.openbanking.mkb;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.FintechblocksAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentCapabilities(generateFromImplementedExecutors = true)
public final class MkbAgent extends FintechblocksAgent {

    @Inject
    public MkbAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
    }

    protected String getIntegrationName() {
        return MkbConstants.INTEGRATION_NAME;
    }
}
