package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.sydbank;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CHECKING_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.BankdataAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentCapabilities({CHECKING_ACCOUNTS})
public final class SydbankAgent extends BankdataAgent {

    @Inject
    public SydbankAgent(AgentComponentProvider componentProvider) {
        super(componentProvider, SydbankConstants.BASE_URL, SydbankConstants.BASE_AUTH_URL);
    }
}
