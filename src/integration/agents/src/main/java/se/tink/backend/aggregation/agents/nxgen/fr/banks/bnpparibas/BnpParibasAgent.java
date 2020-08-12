package se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas;

import com.google.inject.Inject;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

public class BnpParibasAgent extends BnpParibasAgentBase {

    @Inject
    public BnpParibasAgent(AgentComponentProvider componentProvider) {
        super(componentProvider, new BnpParibasConfiguration());
    }
}
