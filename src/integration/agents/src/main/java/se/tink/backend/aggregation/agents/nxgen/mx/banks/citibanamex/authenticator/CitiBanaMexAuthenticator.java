package se.tink.backend.aggregation.agents.nxgen.mx.banks.citibanamex.authenticator;

import java.time.LocalDate;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.citibanamex.CitiBanaMexApiClient;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.citibanamex.CitiBanaMexConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.citibanamex.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.utils.random.RandomUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class CitiBanaMexAuthenticator implements PasswordAuthenticator {

    private final CitiBanaMexApiClient client;
    private final SessionStorage sessionStorage;

    public CitiBanaMexAuthenticator(CitiBanaMexApiClient client, SessionStorage sessionStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void authenticate(String username, String password)
            throws AuthenticationException, AuthorizationException {
        // Identifiers are used in subsequent requests.
        createIdentifiers();

        client.getClientName(username);
        LoginResponse loginResponse = client.login(password);

        sessionStorage.put(Storage.HOLDER_NAME, loginResponse.getClientName());
    }

    private void createIdentifiers() {
        sessionStorage.put(Storage.RSA_APPLICATION_KEY, generateDeviceId().toUpperCase());
        sessionStorage.put(Storage.HARDWARE_ID, generateDeviceId());
        sessionStorage.put(Storage.DEVICE_ID, generateDeviceId());
        sessionStorage.put(Storage.TIMESTAMP, LocalDate.now());
    }

    public static String generateDeviceId() {
        return RandomUtils.generateRandomHexEncoded(16);
    }
}
