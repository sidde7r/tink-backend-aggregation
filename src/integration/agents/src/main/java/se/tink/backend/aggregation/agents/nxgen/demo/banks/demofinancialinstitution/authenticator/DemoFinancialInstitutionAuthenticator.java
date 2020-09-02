package se.tink.backend.aggregation.agents.nxgen.demo.banks.demofinancialinstitution.authenticator;

import java.util.HashMap;
import java.util.Map;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
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
            throws AuthenticationException, AuthorizationException {

        Map<String, String> userCredentials = new HashMap<>();
        userCredentials.put(
                DemoFinancialInstitutionLoginCredentials.USER1_USERNAME,
                DemoFinancialInstitutionLoginCredentials.USER1_PASSWORD);
        userCredentials.put(
                DemoFinancialInstitutionLoginCredentials.USER2_USERNAME,
                DemoFinancialInstitutionLoginCredentials.USER2_PASSWORD);
        userCredentials.put(
                DemoFinancialInstitutionLoginCredentials.USER3_USERNAME,
                DemoFinancialInstitutionLoginCredentials.USER3_PASSWORD);

        if (userExists(userCredentials, username)
                && userCredentials.get(username).equals(password)) {
            putInSessionStorage(sessionStorage, username, password);
        } else throw ThirdPartyAppError.AUTHENTICATION_ERROR.exception();
    }

    private boolean userExists(Map<String, String> userCredentials, String username) {
        return username != null && userCredentials.containsKey(username);
    }

    private static void putInSessionStorage(
            SessionStorage sessionStorage, String username, String password) {
        sessionStorage.put(Storage.BASIC_AUTH_USERNAME, username);
        sessionStorage.put(Storage.BASIC_AUTH_PASSWORD, password);
    }
}
