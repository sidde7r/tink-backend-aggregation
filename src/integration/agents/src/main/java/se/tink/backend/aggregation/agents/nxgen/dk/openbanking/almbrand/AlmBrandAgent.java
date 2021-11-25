package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.almbrand;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.BankdataAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentCapabilities({CHECKING_ACCOUNTS})
public final class AlmBrandAgent extends BankdataAgent {

    @Inject
    public AlmBrandAgent(AgentComponentProvider componentProvider) {
        super(componentProvider, AlmBrandConstants.BASE_URL, AlmBrandConstants.BASE_AUTH_URL);
    }
}
