package se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.authenticator;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.IcaBankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.icabanken.IcaBankenConstants.StorageKeys;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class IcaBankenSandboxAuthenticator implements Authenticator {

    private final IcaBankenApiClient apiClient;
    private final SessionStorage sessionStorage;

    public IcaBankenSandboxAuthenticator(
            IcaBankenApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void authenticate(Credentials credentials) {
        sessionStorage.put(StorageKeys.TOKEN, apiClient.authenticate());
    }
}
