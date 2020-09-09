package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske;

import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.filters.JyskeKnownErrorsFilter;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.session.JyskeSessionHandler;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankdataAgent;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class JyskeAbstractAgent extends BankdataAgent {

    protected final JyskeApiClient apiClient;

    public JyskeAbstractAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
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
