package se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.authenticator;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.ImaginBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.ImaginBankConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.ImaginBankConstants.DefaultRequestParams;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.ImaginBankSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.authenticator.rpc.ImaginSessionResponse;
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
        SessionResponse sessionResponse = apiClient.initializeSession(username);
        ImaginSessionResponse imaginSessionResponse = sessionResponse.getResImagin();

        if (imaginSessionResponse == null) {
            throw LoginError.NOT_SUPPORTED.exception(
                    "Unsupported flow for userType = " + sessionResponse.getUserType());
        }

        // Initialize password hasher with seed from initialization request.
        final String passwordHash =
                LaCaixaPasswordHash.hash(
                        imaginSessionResponse.getSeed(),
                        Integer.parseInt(imaginSessionResponse.getIterations()),
                        password);

        // Construct login request from username and hashed password
        LoginResponse loginResponse =
                apiClient.login(
                        new LoginRequest(
                                username,
                                sessionResponse.getUserType(),
                                DefaultRequestParams.EXISTS_USER,
                                passwordHash,
                                ImaginBankConstants.DefaultRequestParams.ALTA_IMAGINE,
                                ImaginBankConstants.DefaultRequestParams.DEMO));

        sessionStorage.setLoginResponse(loginResponse);
    }
}
