package se.tink.backend.aggregation.agents.nxgen.demo.banks.demofakebank.authenticator;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.demofakebank.DemoFakeBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.demofakebank.DemoFakeBankConstants;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.demofakebank.authenticator.rpc.DemoFakeBankAuthenticationBody;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class DemoFakeBankAuthenticator implements PasswordAuthenticator {
    private DemoFakeBankApiClient client;
    private SessionStorage sessionStorage;

    public DemoFakeBankAuthenticator(DemoFakeBankApiClient client, SessionStorage sessionStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void authenticate(String username, String password) throws AuthenticationException, AuthorizationException {
        try {
            DemoFakeBankAuthenticateResponse response = client.authenticate(
                    new DemoFakeBankAuthenticationBody(username, password));

            if(response.getStatus() != null && response.getStatus().equals(DemoFakeBankConstants.Responses.SUCCESS_STRING)) {
                sessionStorage.put(DemoFakeBankConstants.Storage.AUTH_TOKEN, response.getToken());
                sessionStorage.put(DemoFakeBankConstants.Storage.USERNAME, username);
            }
        } catch (HttpResponseException e) {
            handleAuthenticationException(e);
        }
    }

    private void handleAuthenticationException(HttpResponseException e) throws LoginException {
        HttpResponse response = e.getResponse();
        //TODO: Handle exception!
        throw e;
    }
}
