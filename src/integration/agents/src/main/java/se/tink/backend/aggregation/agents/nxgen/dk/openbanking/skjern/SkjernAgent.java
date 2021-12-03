package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.skjern;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CHECKING_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.BankdataAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentCapabilities({CHECKING_ACCOUNTS})
public final class SkjernAgent extends BankdataAgent {

    @Inject
    public SkjernAgent(AgentComponentProvider componentProvider) {
        super(componentProvider, SkjernConstants.BASE_URL, SkjernConstants.BASE_AUTH_URL);
    }
}
