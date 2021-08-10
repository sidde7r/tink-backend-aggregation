package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske;

import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.filters.JyskeKnownErrorsFilter;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.session.JyskeSessionHandler;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankdataAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemIdIFrameControllerInitializer;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;

public abstract class JyskeAbstractAgent extends BankdataAgent {

    protected final JyskeApiClient apiClient;

    public JyskeAbstractAgent(
            AgentComponentProvider agentComponentProvider,
            NemIdIFrameControllerInitializer iFrameControllerInitializer) {
        super(agentComponentProvider, iFrameControllerInitializer);
        this.apiClient = new JyskeApiClient(client);
    }

    @Override
    protected Filter constructKnowErrorsFilter() {
        return new JyskeKnownErrorsFilter();
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new JyskeSessionHandler(
                apiClient, credentials, new JyskePersistentStorage(persistentStorage));
    }
}
