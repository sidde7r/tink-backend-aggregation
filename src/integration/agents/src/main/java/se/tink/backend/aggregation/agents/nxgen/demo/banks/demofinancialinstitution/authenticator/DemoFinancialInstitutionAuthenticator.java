package se.tink.backend.aggregation.agents.nxgen.demo.banks.demofinancialinstitution.authenticator;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.demofinancialinstitution.DemoFinancialInstitutionApiClient;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.demofinancialinstitution.DemoFinancialInstitutionConstants;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.demofinancialinstitution.authenticator.rpc.DemoFinancialInstitutionAuthenticationBody;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class DemoFinancialInstitutionAuthenticator implements PasswordAuthenticator {
    private DemoFinancialInstitutionApiClient client;
    private SessionStorage sessionStorage;

    public DemoFinancialInstitutionAuthenticator(DemoFinancialInstitutionApiClient client, SessionStorage sessionStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void authenticate(String username, String password)
            throws AuthenticationException, AuthorizationException {
        try {
            DemoFinancialInstitutionAuthenticateResponse response =
                    client.authenticate(new DemoFinancialInstitutionAuthenticationBody(username, password));

            if (response.getStatus() != null
                    && response.getStatus()
                            .equals(DemoFinancialInstitutionConstants.Responses.SUCCESS_STRING)) {
                sessionStorage.put(DemoFinancialInstitutionConstants.Storage.AUTH_TOKEN, response.getToken());
                sessionStorage.put(DemoFinancialInstitutionConstants.Storage.USERNAME, username);
            }
        } catch (HttpResponseException e) {
            handleAuthenticationException(e);
        }
    }

    private void handleAuthenticationException(HttpResponseException e) throws LoginException {
        HttpResponse response = e.getResponse();
        // TODO: Handle exception!
        throw e;
    }
}
