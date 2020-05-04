package se.tink.backend.aggregation.agents.nxgen.demo.banks.demofinancialinstitution.authenticator;

import java.util.HashMap;
import java.util.Map;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.demofinancialinstitution.DemoFinancialInstitutionConstants.DemoFinancialInstitutionLoginCredentials;
import se.tink.backend.aggregation.agents.nxgen.demo.banks.demofinancialinstitution.DemoFinancialInstitutionConstants.Storage;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class DemoFinancialInstitutionAuthenticator implements PasswordAuthenticator {
    private SessionStorage sessionStorage;

    public DemoFinancialInstitutionAuthenticator(SessionStorage sessionStorage) {
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void authenticate(final String username, final String password)
            throws AuthenticationException, AuthorizationException, LoginException {

        Map<String, String> user_credentials = new HashMap<String, String>();
        user_credentials.put(
                DemoFinancialInstitutionLoginCredentials.USER1_USERNAME,
                DemoFinancialInstitutionLoginCredentials.USER1_PASSWORD);
        user_credentials.put(
                DemoFinancialInstitutionLoginCredentials.USER2_USERNAME,
                DemoFinancialInstitutionLoginCredentials.USER2_PASSWORD);
        user_credentials.put(
                DemoFinancialInstitutionLoginCredentials.USER3_USERNAME,
                DemoFinancialInstitutionLoginCredentials.USER3_PASSWORD);

        if (!username.equals(null) && user_credentials.containsKey(username)) {
            if (user_credentials.get(username).equals(password)) {
                putInSessionStorage(sessionStorage, username, password);
            } else throw ThirdPartyAppError.AUTHENTICATION_ERROR.exception();
        }
    }

    public static void putInSessionStorage(
            SessionStorage sessionStorage, String username, String password) {
        sessionStorage.put(Storage.BASIC_AUTH_USERNAME, username);
        sessionStorage.put(Storage.BASIC_AUTH_PASSWORD, password);
    }
}
