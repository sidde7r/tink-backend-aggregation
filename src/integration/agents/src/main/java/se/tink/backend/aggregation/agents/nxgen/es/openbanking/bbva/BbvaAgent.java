package se.tink.backend.aggregation.agents.nxgen.es.openbanking.bbva;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.TRANSFERS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentPisCapability;
import se.tink.backend.aggregation.client.provider_configuration.rpc.PisCapability;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentPisCapability(
        capabilities = {
            PisCapability.SEPA_CREDIT_TRANSFER,
            PisCapability.SEPA_INSTANT_CREDIT_TRANSFER,
            PisCapability.PIS_FUTURE_DATE
        })
@AgentCapabilities({CHECKING_ACCOUNTS, TRANSFERS})
public final class BbvaAgent
        extends se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bbva
                .BbvaAgent {
    @Inject
    public BbvaAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
    }
}
