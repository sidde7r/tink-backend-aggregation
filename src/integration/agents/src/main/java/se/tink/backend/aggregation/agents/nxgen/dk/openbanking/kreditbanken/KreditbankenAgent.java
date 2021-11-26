package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.kreditbanken;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.BankdataAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentCapabilities({CHECKING_ACCOUNTS})
public final class KreditbankenAgent extends BankdataAgent {

    @Inject
    public KreditbankenAgent(AgentComponentProvider componentProvider) {
        super(
                componentProvider,
                KreditbankenConstants.BASE_URL,
                KreditbankenConstants.BASE_AUTH_URL);
    }
}
