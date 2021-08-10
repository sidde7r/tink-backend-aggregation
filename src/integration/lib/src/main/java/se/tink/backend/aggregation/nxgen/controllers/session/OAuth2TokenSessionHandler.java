package se.tink.backend.aggregation.nxgen.controllers.session;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RequiredArgsConstructor
public class OAuth2TokenSessionHandler implements SessionHandler {

    private final PersistentStorage persistentStorage;

    @Override
    public void logout() {
        // NOOP.
    }

    @Override
    public void keepAlive() throws SessionException {
        OAuth2Token token =
                persistentStorage
                        .get(OAuth2Constants.PersistentStorageKeys.OAUTH_2_TOKEN, OAuth2Token.class)
                        .orElseThrow(SessionError.SESSION_EXPIRED::exception);

        if (token.hasAccessExpired()) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
