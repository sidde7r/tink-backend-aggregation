package se.tink.backend.aggregation.agents.nxgen.demo.banks.demoFI.authenticator;

import com.fasterxml.jackson.core.JsonProcessingException;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.demoFI.DemoFakeBankApiClient;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.demoFI.DemoFakeBankConstants;

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
            DemoFakeBankAuthenticateResponse response = client.authenticate(username, password);

            if(response.getStatus() == "SUCCEED") {
                sessionStorage.put(DemoFakeBankConstants.Storage.AUTH_TOKEN, response.getToken());
            }
        } catch (HttpResponseException e) {
            HandleAuthenticationException(e);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Credentials are not correct`");
        }
    }

    private void HandleAuthenticationException(HttpResponseException e) throws LoginException {
        HttpResponse response = e.getResponse();
        //TODO: Handle exception!
        throw e;
    }
}
