package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator;

import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.AxaApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.AxaStorage;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticator;

public final class AxaAutoAuthenticator implements AutoAuthenticator {
    private AxaApiClient apiClient;
    private AxaStorage storage;

    public AxaAutoAuthenticator(AxaApiClient apiClient, AxaStorage storage) {
        this.apiClient = apiClient;
        this.storage = storage;
    }

    @Override
    public void autoAuthenticate()
            throws SessionException, BankServiceException, AuthorizationException {
        AxaCommonAuthenticator.authenticate(apiClient, storage);
    }
}
