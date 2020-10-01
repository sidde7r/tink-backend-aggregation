package se.tink.backend.aggregation.agents.nxgen.fr.creditcards.amex;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Agent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentCapabilities({CREDIT_CARDS})
public final class AmericanExpressFRAgent extends AmericanExpressV62Agent {

    @Inject
    public AmericanExpressFRAgent(AgentComponentProvider agentComponentProvider) {
        super(agentComponentProvider, new AmericanExpressFRConfiguration());
    }
}
