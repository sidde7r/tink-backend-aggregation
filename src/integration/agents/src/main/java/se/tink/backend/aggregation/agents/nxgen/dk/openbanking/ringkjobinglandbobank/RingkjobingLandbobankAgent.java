package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.ringkjobinglandbobank;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CHECKING_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.BankdataAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentCapabilities({CHECKING_ACCOUNTS})
public final class RingkjobingLandbobankAgent extends BankdataAgent {

    @Inject
    public RingkjobingLandbobankAgent(AgentComponentProvider componentProvider) {
        super(
                componentProvider,
                RingkjobingLandbobankConstants.BASE_URL,
                RingkjobingLandbobankConstants.BASE_AUTH_URL);
    }
}
