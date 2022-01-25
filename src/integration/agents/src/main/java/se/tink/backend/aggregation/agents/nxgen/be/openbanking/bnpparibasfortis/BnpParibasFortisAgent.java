package se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CHECKING_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibasfortisbase.BnpParibasFortisBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibasfortisbase.configuration.BnpParibasFortisBaseBankConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentCapabilities({CHECKING_ACCOUNTS})
public class BnpParibasFortisAgent extends BnpParibasFortisBaseAgent {

    @Inject
    public BnpParibasFortisAgent(AgentComponentProvider agentComponentProvider) {
        super(
                agentComponentProvider,
                new BnpParibasFortisBaseBankConfiguration(
                        "https://regulatory.api.bnpparibasfortis.be",
                        "https://services.bnpparibasfortis.be/SEPLJ04/sps/oauth/oauth20/authorize"));
    }
}
