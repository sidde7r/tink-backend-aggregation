package se.tink.backend.aggregation.agents.nxgen.uk.creditcards.amex.v62;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.IDENTITY_DATA;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Agent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentCapabilities({CREDIT_CARDS, IDENTITY_DATA})
public final class AmericanExpressV62UKAgent extends AmericanExpressV62Agent {

    @Inject
    public AmericanExpressV62UKAgent(AgentComponentProvider componentProvider) {
        super(componentProvider, new AmericanExpressV62UKConfiguration());
    }
}
