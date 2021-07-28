package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.triodos;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.triodos.authenticator.ConsentStatusFetcher;
import se.tink.backend.aggregation.nxgen.controllers.session.OAuth2TokenSessionHandler;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class TriodosSessionHandler extends OAuth2TokenSessionHandler {

    private final ConsentStatusFetcher consentStatusFetcher;

    public TriodosSessionHandler(
            PersistentStorage persistentStorage, ConsentStatusFetcher consentStatusFetcher) {
        super(persistentStorage);
        this.consentStatusFetcher = consentStatusFetcher;
    }

    @Override
    public void keepAlive() throws SessionException {
        super.keepAlive();
        consentStatusFetcher.validateConsent();
    }
}
