package se.tink.backend.aggregation.agents.nxgen.it.openbanking.bper;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.TRANSFERS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.PisCapability.*;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentPisCapability;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, TRANSFERS})
@AgentPisCapability(
        capabilities = {
            SEPA_CREDIT_TRANSFER,
            SEPA_INSTANT_CREDIT_TRANSFER,
            PIS_SEPA_RECURRING_PAYMENTS
        })
public final class BperAgent extends CbiGlobeAgent {

    @Inject
    public BperAgent(AgentComponentProvider agentComponentProvider) {
        super(agentComponentProvider);
    }
}
