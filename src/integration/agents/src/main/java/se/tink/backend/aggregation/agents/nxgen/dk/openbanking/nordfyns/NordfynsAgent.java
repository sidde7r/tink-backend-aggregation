package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.nordfyns;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.BankdataAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentCapabilities({CHECKING_ACCOUNTS})
public final class NordfynsAgent extends BankdataAgent {

    @Inject
    public NordfynsAgent(AgentComponentProvider componentProvider) {
        super(componentProvider, NordfynsConstants.BASE_URL, NordfynsConstants.BASE_AUTH_URL);
    }
}
