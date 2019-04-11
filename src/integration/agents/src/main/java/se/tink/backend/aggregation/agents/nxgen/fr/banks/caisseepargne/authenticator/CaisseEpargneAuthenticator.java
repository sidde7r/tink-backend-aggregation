package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.authenticator;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.CaisseEpargneApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.CaisseEpargneConstants;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.authenticator.rpc.AuthenticationRequest;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.authenticator.rpc.AuthenticationResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class CaisseEpargneAuthenticator implements PasswordAuthenticator {

    private final CaisseEpargneApiClient apiClient;
    private final PersistentStorage persistentStorage;

    public CaisseEpargneAuthenticator(
            CaisseEpargneApiClient apiClient, PersistentStorage persistentStorage) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
    }

    @Override
    public void authenticate(String username, String password)
            throws AuthenticationException, AuthorizationException {

        AuthenticationRequest request = new AuthenticationRequest();
        request.setUsername(username);
        request.setPassword(password);
        request.setDeviceId(persistentStorage.get(CaisseEpargneConstants.StorageKey.DEVICE_ID));

        AuthenticationResponse response = apiClient.authenticate(request);

        if (!response.isResponseOK()) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }
    }
}
