package se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.authenticator;

import java.util.Map;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.SantanderApiClient;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.SantanderConstants.RESPONSE_CODES;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.SantanderConstants.STORAGE;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.fetcher.ApiResponse;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.santander.fetcher.Fields.Session;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.i18n.LocalizableKey;

public class SantanderPasswordAuthenticator implements PasswordAuthenticator {

    private final SantanderApiClient apiClient;
    private final SessionStorage sessionStorage;

    public SantanderPasswordAuthenticator(
            SantanderApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void authenticate(String username, String password)
            throws AuthenticationException, AuthorizationException {
        ApiResponse<Map<String, String>> sessionResponse =
                apiClient.fetchAuthToken(username, password);

        if (!sessionResponse.getCode().equals(RESPONSE_CODES.OK)) {
            handleErrorResponse(sessionResponse);
        }

        String sessionToken = sessionResponse.getBusinessData().get(0).get(Session.SESSION_TOKEN);
        sessionStorage.put(STORAGE.SESSION_TOKEN, sessionToken);
    }

    private void handleErrorResponse(ApiResponse<Map<String, String>> response)
            throws LoginException {

        if (response.getCode().equals(RESPONSE_CODES.INCORRECT_CREDENTIALS)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception(
                    new LocalizableKey(
                            String.format(
                                    "Your bank has responded with the following message '%s'",
                                    response.getMessage())));
        } else {
            throw new IllegalStateException("Unknown authentication error");
        }
    }
}
