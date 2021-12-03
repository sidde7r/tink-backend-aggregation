package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.jyske;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CHECKING_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.BankdataAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentCapabilities({CHECKING_ACCOUNTS})
public final class JyskeAgent extends BankdataAgent {

    @Inject
    public JyskeAgent(AgentComponentProvider componentProvider) {
        super(componentProvider, JyskeConstants.BASE_URL, JyskeConstants.BASE_AUTH_URL);
    }
}
