package se.tink.backend.aggregation.agents.nxgen.de.openbanking.commerzbank;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersTransactionalAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

public final class CommerzBankAgent extends Xs2aDevelopersTransactionalAgent {

    @Inject
    public CommerzBankAgent(AgentComponentProvider componentProvider) {
        super(componentProvider, "https://psd2.api.commerzbank.com");
    }
}
