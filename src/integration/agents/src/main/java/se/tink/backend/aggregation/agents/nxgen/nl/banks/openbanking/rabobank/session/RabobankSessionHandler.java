package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.session;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.RabobankConstants.StorageKey;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RequiredArgsConstructor
public class RabobankSessionHandler implements SessionHandler {

    private final PersistentStorage persistentStorage;

    @Override
    public void logout() {
        // NOOP.
    }

    @Override
    public void keepAlive() throws SessionException {
        OAuth2Token token =
                persistentStorage
                        .get(StorageKey.OAUTH_TOKEN, OAuth2Token.class)
                        .orElseThrow(SessionError.SESSION_EXPIRED::exception);

        if (token.hasAccessExpired()) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
