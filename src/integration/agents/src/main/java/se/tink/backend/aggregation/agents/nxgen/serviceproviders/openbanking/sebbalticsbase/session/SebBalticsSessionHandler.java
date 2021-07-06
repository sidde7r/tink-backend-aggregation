package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.session;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.SebBalticsBaseApiClient;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.PersistentStorageKeys;
import se.tink.backend.aggregation.nxgen.controllers.authentication.oauth.OAuth2Token;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class SebBalticsSessionHandler implements SessionHandler {
    private final SebBalticsBaseApiClient apiClient;
    private final PersistentStorage persistentStorage;

    public SebBalticsSessionHandler(
            SebBalticsBaseApiClient apiClient, PersistentStorage persistentStorage) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
    }

    @Override
    public void logout() {}

    @Override
    public void keepAlive() throws SessionException {
        persistentStorage
                .get(PersistentStorageKeys.OAUTH_2_TOKEN, OAuth2Token.class)
                .filter(t -> !t.hasAccessExpired())
                .orElseThrow(() -> SessionError.SESSION_EXPIRED.exception());
    }
}
