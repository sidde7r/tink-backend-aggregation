package se.tink.backend.aggregation.agents.nxgen.fr.banks.hellobank;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.BnpParibasAgentBase;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

public class HelloBankAgent extends BnpParibasAgentBase {

    @Inject
    public HelloBankAgent(AgentComponentProvider componentProvider) {
        super(componentProvider, new HelloBankConfiguration());
    }
}
