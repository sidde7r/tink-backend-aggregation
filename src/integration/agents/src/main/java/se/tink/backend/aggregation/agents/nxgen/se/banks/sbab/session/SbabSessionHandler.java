package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.session;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.SbabApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.SbabConstants.StorageKey;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.authenticator.entities.InvalidateReasonEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.authenticator.rpc.InvalidateTokenRequest;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public final class SbabSessionHandler implements SessionHandler {
    private final SbabApiClient apiClient;
    private final SessionStorage sessionStorage;

    public SbabSessionHandler(SbabApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void logout() {
        final String accessToken = sessionStorage.get(StorageKey.ACCESS_TOKEN);
        final InvalidateTokenRequest invalidateTokenRequest =
                new InvalidateTokenRequest()
                        .withAccessToken(accessToken)
                        .withInvalidateReason(InvalidateReasonEntity.USER_SIGN_OUT);
    }

    @Override
    public void keepAlive() throws SessionException {
        try {
            apiClient.listAccounts();
        } catch (Exception e) {
            throw new SessionException(SessionError.SESSION_EXPIRED);
        }
    }
}
