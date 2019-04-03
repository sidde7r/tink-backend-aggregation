package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.session;

import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.AktiaApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.AktiaConstants;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public final class AktiaSessionHandler implements SessionHandler {
    private final AktiaApiClient apiClient;
    private final SessionStorage sessionStorage;

    public AktiaSessionHandler(AktiaApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void logout() {}

    @Override
    public void keepAlive() throws SessionException {
        //TODO: Condition will change, because there are no tokens right now
        if (Strings.isNullOrEmpty(this.sessionStorage.get(AktiaConstants.StorageKeys.CLIENT_ID))
                && Strings.isNullOrEmpty(
                        this.sessionStorage.get(AktiaConstants.StorageKeys.CLIENT_SECRET))) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
