package se.tink.backend.aggregation.agents.nxgen.demo.banks.demofinancialinstitution.authenticator;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
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

        sessionStorage.put(Storage.BASIC_AUTH_USERNAME, username);
        sessionStorage.put(Storage.BASIC_AUTH_PASSWORD, password);
    }
}
