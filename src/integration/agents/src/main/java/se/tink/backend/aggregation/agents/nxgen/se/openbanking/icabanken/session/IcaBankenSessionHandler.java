package se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.session;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.IcaBankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.IcaBankenConstants;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public final class IcaBankenSessionHandler implements SessionHandler {
    private final IcaBankenApiClient apiClient;
    private final SessionStorage sessionStorage;

    public IcaBankenSessionHandler(IcaBankenApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void logout() {}

    @Override
    public void keepAlive() throws SessionException {
        if (Strings.isNullOrEmpty(this.sessionStorage.get(IcaBankenConstants.StorageKeys.TOKEN)))
            throw SessionError.SESSION_EXPIRED.exception();
    }
}
