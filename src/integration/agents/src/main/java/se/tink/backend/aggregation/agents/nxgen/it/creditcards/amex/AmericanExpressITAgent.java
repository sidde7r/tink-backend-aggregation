package se.tink.backend.aggregation.agents.nxgen.it.creditcards.amex;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Agent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentCapabilities({CREDIT_CARDS})
public final class AmericanExpressITAgent extends AmericanExpressV62Agent {

    @Inject
    public AmericanExpressITAgent(AgentComponentProvider componentProvider) {
        super(componentProvider, new AmericanExpressITConfiguration());
    }
}
