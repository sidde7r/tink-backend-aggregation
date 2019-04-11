package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken;

import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.storage.IcaBankenSessionStorage;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

public class IcaBankenSessionHandler implements SessionHandler {

    private final IcaBankenApiClient apiClient;
    private final IcaBankenSessionStorage icaBankenSessionStorage;

    public IcaBankenSessionHandler(
            IcaBankenApiClient apiClient, IcaBankenSessionStorage icaBankenSessionStorage) {
        this.apiClient = apiClient;
        this.icaBankenSessionStorage = icaBankenSessionStorage;
    }

    @Override
    public void logout() {}

    @Override
    public void keepAlive() throws SessionException {
        if (Strings.isNullOrEmpty(icaBankenSessionStorage.getSessionId())) {
            throw SessionError.SESSION_EXPIRED.exception();
        }

        try {
            // The keep alive keeps the session alive but doesn't indicate when the session has
            // expired,
            // this is why we also try to fetch accounts
            apiClient.keepAlive();
            apiClient.fetchAccounts();
        } catch (HttpResponseException e) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
