package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.handelsbanken;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.eidasidentity.identity.EidasIdentity;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS})
public final class HandelsbankenAgent extends HandelsbankenUKBaseAgent {

    @Inject
    public HandelsbankenAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
    }

    @Override
    protected EidasIdentity getEidasIdentity() {
        return new EidasIdentity(
                context.getClusterId(), context.getAppId(), "DEFAULT", null, getAgentClass());
    }
}
