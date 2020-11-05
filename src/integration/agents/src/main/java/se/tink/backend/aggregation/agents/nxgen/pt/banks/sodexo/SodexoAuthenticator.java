package se.tink.backend.aggregation.agents.nxgen.pt.banks.sodexo;

import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.sodexo.rpc.AuthenticationResponse;
import se.tink.backend.aggregation.agents.utils.random.RandomUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;

@AllArgsConstructor
public class SodexoAuthenticator implements PasswordAuthenticator {

    private SodexoApiClient sodexoApiClient;
    private SodexoStorage sodexoStorage;

    @Override
    public void authenticate(String username, String password)
            throws AuthenticationException, AuthorizationException {

        if (sodexoStorage.isRegistered() && sodexoApiClient.checkPreloginStatus()) {
            loginWithPin();
        } else {
            firstLogin(username, password);
        }
    }

    private void firstLogin(String nif, String password) {
        AuthenticationResponse authenticationResponse =
                sodexoApiClient.authenticateWithCredentials(nif, password);
        sodexoStorage.setSessionToken(authenticationResponse.getSessionToken());
        sodexoStorage.setUserToken(authenticationResponse.getUserToken());
        String pin = randomPin(4);
        sodexoStorage.setPin(pin);
        sodexoApiClient.setupNewPin(pin);
        sodexoApiClient.authenticateWithPin(pin);
        sodexoStorage.setName(authenticationResponse.getFirstName());
        sodexoStorage.setSurname(authenticationResponse.getSurname());
        sodexoStorage.setCardNumber(authenticationResponse.getCardNumber());
    }

    private void loginWithPin() {
        String pin = sodexoStorage.getPin();
        AuthenticationResponse authenticationResponse = sodexoApiClient.authenticateWithPin(pin);
        sodexoStorage.setSessionToken(authenticationResponse.getSessionToken());
    }

    private String randomPin(int numOfDigits) {
        int randomNumber = RandomUtils.randomInt((int) Math.pow(10, numOfDigits));
        return String.format("%04d", randomNumber);
    }
}
