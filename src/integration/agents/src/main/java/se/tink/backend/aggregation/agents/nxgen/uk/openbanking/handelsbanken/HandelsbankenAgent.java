package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.handelsbanken;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS})
public final class HandelsbankenAgent extends HandelsbankenUKBaseAgent {

    @Inject
    public HandelsbankenAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
    }
}
