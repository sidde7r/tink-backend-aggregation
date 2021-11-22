package se.tink.backend.aggregation.agents.nxgen.fi.banks.saastopankki;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.LOANS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.SamlinkAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.SamlinkV1Configuration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentCapabilities({CHECKING_ACCOUNTS, CREDIT_CARDS, LOANS})
public final class SpAgent extends SamlinkAgent {

    @Inject
    public SpAgent(AgentComponentProvider componentProvider) {
        super(componentProvider, new SamlinkV1Configuration(SpConstants.Url.BASE));
    }
}
