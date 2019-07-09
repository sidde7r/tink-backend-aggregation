package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkortv2.session;

import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkortv2.SebKortApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkortv2.SebKortConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkortv2.exceptions.SebKortUnexpectedMediaTypeException;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class SebKortSessionHandler implements SessionHandler {
    private final SebKortApiClient apiClient;
    private final SessionStorage sessionStorage;

    public SebKortSessionHandler(SebKortApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void logout() {
        apiClient.logout();
    }

    @Override
    public void keepAlive() throws SessionException {
        if (Strings.isNullOrEmpty(sessionStorage.get(SebKortConstants.StorageKey.AUTHORIZATION))) {
            throw SessionError.SESSION_EXPIRED.exception();
        }

        try {
            apiClient.fetchBillingUnits();
        } catch (HttpResponseException | SebKortUnexpectedMediaTypeException e) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
