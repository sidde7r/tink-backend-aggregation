package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.djurslands;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CHECKING_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.BankdataAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentCapabilities({CHECKING_ACCOUNTS})
public final class DjurslandsAgent extends BankdataAgent {

    @Inject
    public DjurslandsAgent(AgentComponentProvider componentProvider) {
        super(componentProvider, DjurslandsConstants.BASE_URL, DjurslandsConstants.BASE_AUTH_URL);
    }
}
