package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demofinancialinstitution.authenticator;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demofinancialinstitution.DemoFinancialInstitutionConstants.DemoFinancialInstitutionLoginCredentials;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demofinancialinstitution.DemoFinancialInstitutionConstants.Storage;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class DemoFinancialInstitutionAuthenticator implements PasswordAuthenticator {
    public static final int OB_SESSION_LIFETIME = 90;
    private SessionStorage sessionStorage;
    private final Credentials credentials;
    private final Provider provider;

    public DemoFinancialInstitutionAuthenticator(
            SessionStorage sessionStorage, Credentials credentials, Provider provider) {
        this.sessionStorage = sessionStorage;
        this.credentials = credentials;
        this.provider = provider;
    }

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
            if (Provider.AccessType.OPEN_BANKING == provider.getAccessType()) {
                credentials.setSessionExpiryDate(
                        Optional.ofNullable(credentials.getSessionExpiryDate())
                                .map(
                                        d ->
                                                d.toInstant()
                                                        .atZone(ZoneId.systemDefault())
                                                        .toLocalDate())
                                .filter(localDate -> localDate.isAfter(LocalDate.now()))
                                .orElse(LocalDate.now().plusDays(OB_SESSION_LIFETIME)));
            }
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
