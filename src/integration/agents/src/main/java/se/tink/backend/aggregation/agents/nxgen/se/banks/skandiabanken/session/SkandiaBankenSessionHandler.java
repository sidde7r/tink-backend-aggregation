package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.session;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.SkandiaBankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;

public class SkandiaBankenSessionHandler implements SessionHandler {
    private final SkandiaBankenApiClient apiClient;

    public SkandiaBankenSessionHandler(SkandiaBankenApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public void logout() {
        apiClient.logout();
    }

    @Override
    public void keepAlive() throws SessionException {
        try {
            apiClient.fetchIdentityData();
        } catch (HttpResponseException hre) {
            final ErrorResponse error = hre.getResponse().getBody(ErrorResponse.class);
            if (error.isUnauthorized()) {
                throw SessionError.SESSION_EXPIRED.exception();
            }
            throw hre;
        }
    }
}
