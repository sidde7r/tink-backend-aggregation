package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.sparekassensjaellandfyn;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CHECKING_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.BankdataAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentCapabilities({CHECKING_ACCOUNTS})
public final class SparekassenSjaellandFynAgent extends BankdataAgent {

    @Inject
    public SparekassenSjaellandFynAgent(AgentComponentProvider componentProvider) {
        super(
                componentProvider,
                SparekassenSjaellandFynConstants.BASE_URL,
                SparekassenSjaellandFynConstants.BASE_AUTH_URL);
    }
}
