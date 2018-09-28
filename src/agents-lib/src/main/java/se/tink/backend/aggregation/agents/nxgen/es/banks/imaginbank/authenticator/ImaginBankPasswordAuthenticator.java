package se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.authenticator;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.ImaginBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.ImaginBankConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.authenticator.rpc.LoginRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.authenticator.rpc.SessionResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class ImaginBankPasswordAuthenticator implements PasswordAuthenticator {

    private final ImaginBankApiClient bankClient;
    private final SessionStorage sessionStorage;

    public ImaginBankPasswordAuthenticator(ImaginBankApiClient bankClient, SessionStorage sessionStorage) {
        this.bankClient = bankClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void authenticate(String username, String password) throws AuthenticationException, AuthorizationException {

        // Requests a session ID from the server in the form of a cookie.
        // Also gets seed for password hashing.
        SessionResponse sessionResponse = bankClient.initializeSession();


        // Initialize password hasher with seed from initialization request.
        ImaginBankPasswordHash otpHelper = new ImaginBankPasswordHash(sessionResponse.getSeed(),
                Integer.parseInt(sessionResponse.getIterations()),
                password);


        // Construct login request from username and hashed password
        LoginResponse loginResponse = bankClient.login(new LoginRequest(username,
                otpHelper.createOtp(),
                ImaginBankConstants.DefaultRequestParams.DEMO,
                ImaginBankConstants.DefaultRequestParams.ALTA_IMAGINE));

        sessionStorage.put(ImaginBankConstants.Storage.USER_NAME, loginResponse.getUserName());
    }
}
