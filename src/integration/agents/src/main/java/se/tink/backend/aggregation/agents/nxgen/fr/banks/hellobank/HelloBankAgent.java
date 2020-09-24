package se.tink.backend.aggregation.agents.nxgen.fr.banks.hellobank;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.BnpParibasAgentBase;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS})
public final class HelloBankAgent extends BnpParibasAgentBase {

    @Inject
    public HelloBankAgent(AgentComponentProvider componentProvider) {
        super(componentProvider, new HelloBankConfiguration());
    }
}
