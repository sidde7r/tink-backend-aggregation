package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.authenticator;

import java.util.Objects;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.authenticator.rpc.LoginRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.utils.BbvaUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class BbvaAuthenticator implements PasswordAuthenticator {

    private final SessionStorage sessionStorage;
    private BbvaApiClient apiClient;

    public BbvaAuthenticator(BbvaApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void authenticate(String username, String password)
            throws AuthenticationException, AuthorizationException {
        final LoginRequest loginRequest =
                new LoginRequest(BbvaUtils.formatUsername(username), password);

        HttpResponse response = apiClient.login(loginRequest);

        String responseString = response.getBody(String.class);

        if (responseString.toLowerCase().contains(BbvaConstants.Message.LOGIN_SUCCESS)) {
            InitiateSessionResponse initiateSessionResponse = apiClient.initiateSession();
            if (!Objects.equals(
                    initiateSessionResponse.getResult().getCode().toLowerCase(),
                    BbvaConstants.Message.OK)) {
                throw new IllegalStateException(
                        String.format(
                                "Initiate session failed with code %s",
                                initiateSessionResponse.getResult().getCode()));
            }
            sessionStorage.put(
                    BbvaConstants.StorageKeys.HOLDER_NAME, initiateSessionResponse.getName());
        } else if (responseString
                .toLowerCase()
                .contains(BbvaConstants.Message.LOGIN_WRONG_CREDENTIAL_CODE)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        } else {
            throw new IllegalStateException("Could not authenticate");
        }
    }
}
