package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.session;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebBaseApiClient;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.PersistentStorageKeys;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2TokenBase;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public final class SebSessionHandler implements SessionHandler {
    private final SebBaseApiClient apiClient;
    private final PersistentStorage persistentStorage;

    public SebSessionHandler(SebBaseApiClient apiClient, PersistentStorage persistentStorage) {

        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
    }

    @Override
    public void logout() {}

    @Override
    public void keepAlive() throws SessionException {
        persistentStorage
                .get(PersistentStorageKeys.OAUTH_2_TOKEN, OAuth2Token.class)
                .filter(OAuth2TokenBase::canUseAccessToken)
                .orElseThrow(SessionError.SESSION_EXPIRED::exception);
    }
}
