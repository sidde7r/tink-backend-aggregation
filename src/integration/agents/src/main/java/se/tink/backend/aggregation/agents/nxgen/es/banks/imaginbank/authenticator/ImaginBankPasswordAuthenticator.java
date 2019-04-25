package se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.authenticator;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.ImaginBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.ImaginBankConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.ImaginBankSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.authenticator.rpc.LoginRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.authenticator.rpc.SessionResponse;
import se.tink.backend.aggregation.agents.utils.crypto.LaCaixaPasswordHash;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;

public class ImaginBankPasswordAuthenticator implements PasswordAuthenticator {

    private final ImaginBankApiClient apiClient;
    private final ImaginBankSessionStorage sessionStorage;

    public ImaginBankPasswordAuthenticator(
            ImaginBankApiClient apiClient, ImaginBankSessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void authenticate(String username, String password)
            throws AuthenticationException, AuthorizationException {

        // Requests a session ID from the server in the form of a cookie.
        // Also gets seed for password hashing.
        SessionResponse sessionResponse = apiClient.initializeSession();

        // Initialize password hasher with seed from initialization request.
        LaCaixaPasswordHash otpHelper =
                new LaCaixaPasswordHash(
                        sessionResponse.getSeed(),
                        Integer.parseInt(sessionResponse.getIterations()),
                        password);

        // Construct login request from username and hashed password
        LoginResponse loginResponse =
                apiClient.login(
                        new LoginRequest(
                                username,
                                otpHelper.createOtp(),
                                ImaginBankConstants.DefaultRequestParams.DEMO,
                                ImaginBankConstants.DefaultRequestParams.ALTA_IMAGINE));

        sessionStorage.setLoginResponse(loginResponse);
    }
}
