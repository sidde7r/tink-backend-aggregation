package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.authenticator;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.LaCaixaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.authenticator.rpc.LoginRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.authenticator.rpc.SessionResponse;
import se.tink.backend.aggregation.agents.utils.crypto.LaCaixaPasswordHash;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;

public class LaCaixaPasswordAuthenticator implements PasswordAuthenticator {

    private final LaCaixaApiClient bankClient;

    public LaCaixaPasswordAuthenticator(LaCaixaApiClient bankClient) {
        this.bankClient = bankClient;
    }

    @Override
    public void authenticate(String username, String password) throws AuthenticationException, AuthorizationException {

        // Requests a session ID from the server in the form of a cookie.
        // Also gets seed for password hashing.
        SessionResponse sessionResponse = bankClient.initializeSession();


        // Initialize password hasher with seed from initialization request.
        LaCaixaPasswordHash otpHelper = new LaCaixaPasswordHash(sessionResponse.getSeed(),
                Integer.parseInt(sessionResponse.getIterations()),
                password);


        // Construct login request from username and hashed password
        bankClient.login(new LoginRequest(username, otpHelper.createOtp()));
    }
}
