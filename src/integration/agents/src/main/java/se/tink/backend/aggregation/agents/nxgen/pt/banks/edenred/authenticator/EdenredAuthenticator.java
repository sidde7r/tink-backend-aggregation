package se.tink.backend.aggregation.agents.nxgen.pt.banks.edenred.authenticator;

import java.security.SecureRandom;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.edenred.EdenredApiClient;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.edenred.authenticator.rpc.AuthenticationResponse;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.edenred.authenticator.rpc.SetPinResponse;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.edenred.storage.EdenredStorage;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;

@AllArgsConstructor
public class EdenredAuthenticator implements PasswordAuthenticator {

    private EdenredStorage edenredStorage;

    private EdenredApiClient edenredApiClient;

    @Override
    public void authenticate(String username, String password)
            throws AuthenticationException, AuthorizationException {
        if (edenredStorage.isRegistered()) {
            loginUsingPin();
        } else {
            performFirstLogin(username, password);
        }
    }

    private void performFirstLogin(String username, String password) {
        AuthenticationResponse response = edenredApiClient.authenticateDefault(username, password);
        edenredStorage.setToken(response.getData().getToken());
        String pin = generateRandomPin(4);
        SetPinResponse pinResponse = edenredApiClient.setupPin(username, pin);
        edenredStorage.setPin(pin);
        edenredStorage.setUserId(pinResponse.getData().getValue());
    }

    private void loginUsingPin() {
        String userId = edenredStorage.getUserId();
        String pin = edenredStorage.getPin();
        AuthenticationResponse response = edenredApiClient.authenticatePin(userId, pin);
        edenredStorage.setToken(response.getData().getToken());
    }

    private String generateRandomPin(int digits) {
        SecureRandom secureRandom = new SecureRandom();
        int number = secureRandom.nextInt((int) Math.pow(10, digits));
        return String.format("%04d", number);
    }
}
